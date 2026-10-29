package com.monitor.config;

import org.springframework.context.annotation.Configuration;

import com.monitor.service.impl.NotificationServiceImpl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;


/**
 * 通知配置类
 */
@Slf4j
@Configuration
public class NotificationConfig {

    private final NotificationServiceImpl notificationService;

    public NotificationConfig(NotificationServiceImpl notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * 初始化通知服务
     */
    @PostConstruct
    public void initNotificationService() {
        notificationService.initChannels();
        log.info("通知服务初始化完成");
    }
}
