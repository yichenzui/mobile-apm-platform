package com.monitor.config;

import com.monitor.security.JwtAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器注册配置
 *
 * JwtAuthenticationFilter 同时标注了 @Component 且继承自 OncePerRequestFilter，
 * Spring Boot 会自动把它注册为全局 Servlet Filter（对所有 URL 生效），
 * 而 WebSecurityConfig 又把它加入了 Spring Security 过滤链，导致该过滤器被重复执行：
 * 白名单接口（/api/auth/**、/actuator/health 等）会在进入 Security 链之前被提前处理，
 * 造成 permitAll 语义与预期不一致。
 *
 * 这里显式禁用全局自动注册，只保留 Security 过滤链中的那一份。
 */
@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
