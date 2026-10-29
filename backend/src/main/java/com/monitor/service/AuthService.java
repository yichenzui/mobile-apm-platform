package com.monitor.service;

import com.monitor.dto.ChangePasswordDTO;
import com.monitor.dto.LoginDTO;
import com.monitor.dto.RegisterDTO;
import com.monitor.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     * @param registerDTO 注册信息
     */
    void register(RegisterDTO registerDTO);

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的登录结果
     */
    LoginVO refresh(String refreshToken);

    /**
     * 用户退出
     * @param token 访问令牌
     */
    void logout(String token);

    /**
     * 修改密码
     * @param username 用户名
     * @param changePasswordDTO 修改密码信息
     */
    void changePassword(String username, ChangePasswordDTO changePasswordDTO);
}
