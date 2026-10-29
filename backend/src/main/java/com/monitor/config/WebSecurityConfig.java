package com.monitor.config;

import com.monitor.security.JwtAuthenticationEntryPoint;
import com.monitor.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Web安全配置类
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private Environment environment;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 判断是否为开发环境：显式激活 dev，或未指定任何 profile 时默认放行文档
        String[] activeProfiles = environment.getActiveProfiles();
        boolean isDev = activeProfiles.length == 0 || java.util.Arrays.asList(activeProfiles).contains("dev");

        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/api/login").permitAll()
                .requestMatchers("/ai/**").permitAll()
                .requestMatchers("/api/ai/**").permitAll()
                // WebSocket 实时推送端点
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/ws-stomp/**").permitAll()
                .requestMatchers("/api/ws/**").permitAll()
                .requestMatchers("/api/ws-stomp/**").permitAll()
                // 仅开发环境开放 Swagger/API文档（同时兼容 context-path 前缀）
                .requestMatchers(isDev ? "/swagger-ui/**" : "/swagger-not-available").permitAll()
                .requestMatchers(isDev ? "/api/swagger-ui/**" : "/api/swagger-not-available").permitAll()
                .requestMatchers(isDev ? "/v3/api-docs/**" : "/v3/not-available").permitAll()
                .requestMatchers(isDev ? "/api/v3/api-docs/**" : "/api/v3/not-available").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/api/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        // 启用JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOriginPatterns(java.util.Arrays.asList(
            "http://localhost:*",
            "https://localhost:*"
        ));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.Arrays.asList(
            "Authorization",
            "Content-Type",
            "Accept",
            "X-Requested-With"
        ));
        configuration.setAllowCredentials(true);

        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
