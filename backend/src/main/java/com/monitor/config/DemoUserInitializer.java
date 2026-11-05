package com.monitor.config;

import com.monitor.entity.User;
import com.monitor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 演示账号初始化：创建普通用户示例账号（供登录页"普通用户演示账号"快捷登录使用）
 * 仅在账号不存在时创建，不会覆盖已有密码
 */
@Component
@Order(2)
public class DemoUserInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserInitializer.class);

    /** 演示普通用户账号 */
    public static final String DEMO_USERNAME = "demo";
    /** 演示普通用户密码 */
    private static final String DEMO_PASSWORD = "123456";

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public DemoUserInitializer(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (userRepository.findByUsername(DEMO_USERNAME) != null) {
                return;
            }
            User demo = new User();
            demo.setUsername(DEMO_USERNAME);
            demo.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
            demo.setRealName("演示用户");
            demo.setRole("USER");
            demo.setStatus(1);
            demo.setDeleted(false);
            userRepository.insert(demo);
            log.info("演示普通用户已创建：{}/{}", DEMO_USERNAME, DEMO_PASSWORD);
        } catch (Exception e) {
            // 账号已存在（如被其他脚本创建）或表未初始化时不阻塞启动
            log.warn("演示普通用户初始化跳过：{}", e.getMessage());
        }
    }
}
