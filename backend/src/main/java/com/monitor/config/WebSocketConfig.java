package com.monitor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket + STOMP 配置
 * 用于实时推送告警通知、KPI 更新等数据到前端
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅的前缀（服务端推送到这些地址）
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息的前缀（客户端发送到 /app/xxx，服务端 @MessageMapping("/xxx")）
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // STOMP 端点，客户端通过此端点连接 WebSocket
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
