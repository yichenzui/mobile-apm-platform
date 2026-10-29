package com.monitor.config;

import com.monitor.entity.User;
import com.monitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化：仅在首次启动时创建管理员用户（使用默认密码）
 * 后续启动不会修改已有管理员密码，避免覆盖用户自行修改的密码
 */
@Component
public class AdminPasswordInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordInitializer.class);

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // 默认管理员密码
    private static final String DEFAULT_ADMIN_PASSWORD = "123456789";

    // 建库脚本 V1.0.2 写入的初始密码 hash（明文 "123456"）。
    // 若检测到仍是该 hash，说明尚未设置过正式密码，此时才替换为默认密码；
    // 若已被修改（用户自行改过密码），则不再触碰。
    private static final String INITIAL_PASSWORD_HASH =
            "$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW";

    public AdminPasswordInitializer(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        try {
            User admin = userRepository.findByUsername("admin");
            if (admin == null) {
                // 首次启动：创建管理员账户
                User newAdmin = new User();
                newAdmin.setUsername("admin");
                newAdmin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                newAdmin.setRole("ADMIN");
                newAdmin.setStatus(1);
                userRepository.insert(newAdmin);
                log.info("管理员账户已创建（首次初始化），请尽快修改默认密码");
            } else if (INITIAL_PASSWORD_HASH.equals(admin.getPassword())) {
                // 仍是建库脚本写入的初始密码 → 首次真正启用，设为默认密码（仅此一次）
                admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                userRepository.update(admin, true);
                log.info("管理员初始密码已设置为默认密码，请尽快修改");
            } else {
                // 已存在且密码已被修改过：不再覆盖，避免重置用户密码。
                // 需要重置时请手动执行 reset_pwd.bat 或 reset_admin_pwd.sql
                log.info("管理员账户已存在，保留现有密码（重置请执行 reset_pwd.bat）");
            }
        } catch (Exception e) {
            log.error("初始化管理员账户失败: {}", e.getMessage(), e);
        }
    }
}
