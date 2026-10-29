package com.monitor.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.entity.AlertInstance;
import com.monitor.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ObjectMapper objectMapper;
    private final Map<String, NotificationSender> senders = new HashMap<>();

    @Override
    public void sendNotification(AlertInstance alertInstance) {
        try {
            // 解析通知渠道配置（为空时兜底为 email，避免 readTree(null) 抛异常导致通知全部丢失）
            String rawChannels = alertInstance.getNotifyChannels();
            if (rawChannels == null || rawChannels.isBlank()) {
                rawChannels = "[\"email\"]";
            }
            JsonNode channels = objectMapper.readTree(rawChannels);

            // 遍历所有通知渠道
            for (JsonNode channelNode : channels) {
                String channel = channelNode.asText();
                NotificationSender sender = senders.get(channel);

                if (sender != null) {
                    try {
                        boolean success = sender.send(alertInstance);
                        if (success) {
                            log.info("告警通知发送成功，渠道: {}, 告警ID: {}", channel, alertInstance.getId());
                        } else {
                            log.warn("告警通知发送失败，渠道: {}, 告警ID: {}", channel, alertInstance.getId());
                        }
                    } catch (Exception e) {
                        log.error("发送告警通知异常，渠道: {}, 告警ID: {}", channel, alertInstance.getId(), e);
                    }
                } else {
                    log.warn("未找到通知渠道: {}", channel);
                }
            }
        } catch (Exception e) {
            log.error("解析通知渠道配置失败", e);
        }
    }

    @Override
    public void registerChannel(String channel, NotificationSender sender) {
        senders.put(channel, sender);
        log.info("注册通知渠道: {}", channel);
    }

    /**
     * 初始化通知渠道
     */
    public void initChannels() {
        // 注册邮件通知
        registerChannel("email", new EmailNotificationSender());

        // 注册Webhook通知
        registerChannel("webhook", new WebhookNotificationSender());

        // 注册钉钉通知
        registerChannel("dingtalk", new DingTalkNotificationSender());

        log.info("通知渠道初始化完成");
    }

    /**
     * 邮件通知发送器
     */
    private static class EmailNotificationSender implements NotificationSender {
        @Override
        public boolean send(AlertInstance alertInstance) {
            try {
                // TODO: 实现邮件发送逻辑
                log.info("发送邮件通知: {}", alertInstance.getMessage());
                return true;
            } catch (Exception e) {
                log.error("发送邮件通知失败", e);
                return false;
            }
        }
    }

    /**
     * Webhook通知发送器
     */
    private static class WebhookNotificationSender implements NotificationSender {
        @Override
        public boolean send(AlertInstance alertInstance) {
            try {
                // TODO: 实现Webhook发送逻辑
                log.info("发送Webhook通知: {}", alertInstance.getMessage());
                return true;
            } catch (Exception e) {
                log.error("发送Webhook通知失败", e);
                return false;
            }
        }
    }

    /**
     * 钉钉通知发送器
     */
    private static class DingTalkNotificationSender implements NotificationSender {
        @Override
        public boolean send(AlertInstance alertInstance) {
            try {
                // TODO: 实现钉钉通知发送逻辑
                log.info("发送钉钉通知: {}", alertInstance.getMessage());
                return true;
            } catch (Exception e) {
                log.error("发送钉钉通知失败", e);
                return false;
            }
        }
    }
}
