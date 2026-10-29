package com.monitor.controller;

import com.monitor.common.Result;
import com.monitor.dto.ChangePasswordDTO;
import com.monitor.dto.LoginDTO;
import com.monitor.dto.RegisterDTO;
import com.monitor.vo.LoginVO;
import com.monitor.service.AuthService;
import com.monitor.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录系统")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginVO> refresh(@RequestParam String refreshToken) {
        LoginVO loginVO = authService.refresh(refreshToken);
        return Result.success(loginVO);
    }

    /**
     * 用户退出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出登录")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success();
    }

    /**
     * 修改当前登录用户密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码，需提供原密码")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO,
                                       @RequestHeader("Authorization") String token) {
        String jwt = (token != null && token.startsWith("Bearer ")) ? token.substring(7) : token;
        String username = jwtUtil.getUsernameFromToken(jwt);
        authService.changePassword(username, changePasswordDTO);
        return Result.success();
    }
}
