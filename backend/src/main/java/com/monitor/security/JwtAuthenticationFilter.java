package com.monitor.security;

import com.monitor.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token格式为 "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                logger.error("无法获取JWT Token中的用户名", e);
            }
        }

        // 先校验令牌有效性，再查库：
        // 否则 token 已过期/用户已被删除时，loadUserByUsername 抛 UsernameNotFoundException
        // 会绕过 ExceptionTranslationFilter 直接返回 500，而不是 401
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtUtil.validateToken(jwtToken)) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 设置认证
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (UsernameNotFoundException e) {
                // 用户不存在：视为未认证，交给 JwtAuthenticationEntryPoint 返回 401
                logger.warn("JWT 中的用户不存在: " + username);
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
