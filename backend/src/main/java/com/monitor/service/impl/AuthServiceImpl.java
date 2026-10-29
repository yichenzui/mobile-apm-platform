package com.monitor.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.monitor.common.BizException;
import com.monitor.dto.ChangePasswordDTO;
import com.monitor.dto.LoginDTO;
import com.monitor.dto.RegisterDTO;
import com.monitor.entity.User;
import com.monitor.entity.enums.UserRole;
import com.monitor.repository.UserRepository;
import com.monitor.service.AuthService;
import com.monitor.util.JwtUtil;
import com.monitor.vo.LoginVO;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        User user = userRepository.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BizException("账户已被禁用");
        }

        // 生成令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String accessToken = jwtUtil.generateToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRole(user.getRole());

        return loginVO;
    }

    @Override
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existUser = userRepository.findByUsername(registerDTO.getUsername());
        if (existUser != null) {
            throw new BizException("用户名已存在");
        }

        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRoleEnum(UserRole.USER);
        user.setStatus(1);
        // 必须显式设置 deleted=false，否则查询条件 DELETED.eq(0) 匹配不到新用户
        user.setDeleted(false);

        // 保存用户
        userRepository.insert(user);
    }

    @Override
    public LoginVO refresh(String refreshToken) {
        // 验证刷新令牌
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BizException("刷新令牌无效");
        }

        // 从令牌中获取用户信息
        String username = jwtUtil.getUsernameFromToken(refreshToken);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BizException("账户已被禁用");
        }

        // 生成新的令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());

        String newAccessToken = jwtUtil.generateToken(claims);
        String newRefreshToken = jwtUtil.generateRefreshToken(claims);

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(newAccessToken);
        loginVO.setRefreshToken(newRefreshToken);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRole(user.getRole());

        return loginVO;
    }

    @Override
    public void logout(String token) {
        // 实际项目中，可以将令牌加入黑名单
        // 这里简化处理，不做任何操作
    }

    @Override
    public void changePassword(String username, ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        // 管理员暂不支持在线修改密码
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new BizException("管理员暂不支持在线修改密码");
        }

        // 校验原密码
        if (!passwordEncoder.matches(changePasswordDTO.getOldPassword(), user.getPassword())) {
            throw new BizException("原密码错误");
        }

        // 新旧密码不能相同
        if (changePasswordDTO.getOldPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new BizException("新密码不能与旧密码相同");
        }

        // 更新密码（只更新非null字段，避免覆盖其它信息）
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        updateUser.setUpdateTime(LocalDateTime.now());
        userRepository.update(updateUser, true);
    }
}
