package com.monitor.service;

import com.monitor.entity.AlertInstance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 实时推送服务
 * 通过 WebSocket STOMP 协议向前端推送实时告警和数据更新
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealTimePushService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 推送新告警到所有订阅者
     */
    public void pushAlert(AlertInstance alert) {
        // 注意：Map.of 不允许 null 值，这里用 HashMap 并对每个字段做空值兜底，
        // 否则 ruleName/appKey 为空时会抛 NPE，进而导致调用方的事务回滚、告警丢失
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_ALERT");
        payload.put("severity", alert.getSeverity() != null ? alert.getSeverity() : "INFO");
        payload.put("ruleName", alert.getRuleName() != null ? alert.getRuleName() : "");
        payload.put("message", alert.getMessage() != null ? alert.getMessage() : "");
        payload.put("appKey", alert.getAppKey() != null ? alert.getAppKey() : "");
        payload.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/alerts", payload);
        log.debug("实时推送告警: {}", alert.getMessage());
    }

    /**
     * 推送告警状态变更
     */
    public void pushAlertStatusChange(Long alertId, String status, String appKey) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ALERT_STATUS_CHANGE");
        payload.put("alertId", alertId);
        payload.put("status", status != null ? status : "");
        payload.put("appKey", appKey != null ? appKey : "");
        payload.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/alerts/status", payload);
        log.debug("实时推送告警状态变更: {} -> {}", alertId, status);
    }

    /**
     * 推送KPI实时数据更新
     */
    public void pushKpiUpdate(String appKey, Map<String, Object> kpiData) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "KPI_UPDATE");
        payload.put("appKey", appKey != null ? appKey : "");
        payload.put("data", kpiData != null ? kpiData : new HashMap<String, Object>());
        payload.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/kpi/" + appKey, payload);
    }

    /**
     * 推送系统通知
     */
    public void pushSystemNotification(String title, String message, String level) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "SYSTEM_NOTIFICATION");
        payload.put("title", title != null ? title : "");
        payload.put("message", message != null ? message : "");
        payload.put("level", level != null ? level : "INFO");
        payload.put("timestamp", System.currentTimeMillis());
        messagingTemplate.convertAndSend("/topic/notifications", payload);
    }
}
