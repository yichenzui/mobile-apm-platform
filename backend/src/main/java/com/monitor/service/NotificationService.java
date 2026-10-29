package com.monitor.service;

import com.monitor.entity.AlertInstance;

/**
 * 通知服务接口
 */
public interface NotificationService {

    /**
     * 发送通知
     *
     * @param alertInstance 告警实例
     */
    void sendNotification(AlertInstance alertInstance);

    /**
     * 注册通知渠道
     *
     * @param channel 通知渠道
     * @param sender 通知发送器
     */
    void registerChannel(String channel, NotificationSender sender);

    /**
     * 通知发送器接口
     */
    interface NotificationSender {
        /**
         * 发送通知
         *
         * @param alertInstance 告警实例
         * @return 是否发送成功
         */
        boolean send(AlertInstance alertInstance);
    }
}
