package com.monitor.service.impl;

import com.monitor.common.BizException;
import com.monitor.dao.AlertInstanceMapper;
import com.monitor.dto.AlertQueryDTO;
import com.monitor.entity.AlertInstance;
import com.monitor.service.AlertService;
import com.monitor.service.NotificationService;
import com.monitor.service.RealTimePushService;
import com.monitor.vo.AlertRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 告警服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final AlertInstanceMapper alertInstanceMapper;
    private final NotificationService notificationService;
    private final RealTimePushService realTimePushService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendAlertNotification(AlertInstance alertInstance) {
        try {
            // 保存告警实例
            alertInstance.setCreateTime(LocalDateTime.now());
            alertInstance.setUpdateTime(LocalDateTime.now());
            alertInstance.setStatus(AlertInstance.AlertStatus.FIRING);
            alertInstanceMapper.insert(alertInstance);

            // 生成告警消息
            String message = generateAlertMessage(alertInstance);
            alertInstance.setMessage(message);
            // 用 ignoreNulls=true，只更新非 null 字段，避免把未赋值的列刷成 null
            alertInstanceMapper.update(alertInstance, true);

            // 发送通知
            notificationService.sendNotification(alertInstance);

            // 实时推送到前端 WebSocket
            realTimePushService.pushAlert(alertInstance);

            log.info("告警通知发送成功，告警ID: {}", alertInstance.getId());
        } catch (Exception e) {
            log.error("发送告警通知失败", e);
            throw new BizException("发送告警通知失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acknowledgeAlert(Long alertId, String assignee, String note) {
        try {
            AlertInstance alertInstance = alertInstanceMapper.selectOneById(alertId);
            if (alertInstance == null) {
                throw new BizException("告警不存在，ID: " + alertId);
            }

            alertInstance.setStatus(AlertInstance.AlertStatus.ACKNOWLEDGED);
            alertInstance.setAssignee(assignee);
            alertInstance.setProcessNote(note);
            alertInstance.setProcessTime(LocalDateTime.now());
            alertInstance.setUpdateTime(LocalDateTime.now());

            // 用 ignoreNulls=true，只更新非 null 字段，避免把未赋值的列刷成 null
            alertInstanceMapper.update(alertInstance, true);

            // 实时推送状态变更
            realTimePushService.pushAlertStatusChange(alertId, "ACKNOWLEDGED", alertInstance.getAppKey());

            log.info("告警确认成功，告警ID: {}, 处理人: {}", alertId, assignee);
        } catch (Exception e) {
            log.error("确认告警失败", e);
            throw new BizException("确认告警失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveAlert(Long alertId, String assignee, String note) {
        try {
            AlertInstance alertInstance = alertInstanceMapper.selectOneById(alertId);
            if (alertInstance == null) {
                throw new BizException("告警不存在，ID: " + alertId);
            }

            alertInstance.setStatus(AlertInstance.AlertStatus.RESOLVED);
            alertInstance.setAssignee(assignee);
            alertInstance.setProcessNote(note);
            alertInstance.setProcessTime(LocalDateTime.now());
            alertInstance.setEndTime(LocalDateTime.now());
            alertInstance.setUpdateTime(LocalDateTime.now());

            // 用 ignoreNulls=true，只更新非 null 字段，避免把未赋值的列刷成 null
            alertInstanceMapper.update(alertInstance, true);

            // 实时推送状态变更
            realTimePushService.pushAlertStatusChange(alertId, "RESOLVED", alertInstance.getAppKey());

            log.info("告警解决成功，告警ID: {}, 处理人: {}", alertId, assignee);
        } catch (Exception e) {
            log.error("解决告警失败", e);
            throw new BizException("解决告警失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeAlert(Long alertId, String assignee, String note) {
        try {
            AlertInstance alertInstance = alertInstanceMapper.selectOneById(alertId);
            if (alertInstance == null) {
                throw new BizException("告警不存在，ID: " + alertId);
            }

            alertInstance.setStatus(AlertInstance.AlertStatus.CLOSED);
            alertInstance.setAssignee(assignee);
            alertInstance.setProcessNote(note);
            alertInstance.setProcessTime(LocalDateTime.now());
            alertInstance.setEndTime(LocalDateTime.now());
            alertInstance.setUpdateTime(LocalDateTime.now());

            // 用 ignoreNulls=true，只更新非 null 字段，避免把未赋值的列刷成 null
            alertInstanceMapper.update(alertInstance, true);

            // 实时推送状态变更
            realTimePushService.pushAlertStatusChange(alertId, "CLOSED", alertInstance.getAppKey());

            log.info("告警关闭成功，告警ID: {}, 处理人: {}", alertId, assignee);
        } catch (Exception e) {
            log.error("关闭告警失败", e);
            throw new BizException("关闭告警失败", e);
        }
    }

    /**
     * 生成告警消息
     */
    private String generateAlertMessage(AlertInstance alertInstance) {
        String metricName = getMetricDisplayName(alertInstance.getMetric());
        String severityName = getSeverityDisplayName(alertInstance.getSeverity());

        return String.format("[%s] %s告警: %s 当前值: %s, 阈值: %s",
                alertInstance.getAppKey(),
                severityName,
                metricName,
                alertInstance.getCurrentValue(),
                alertInstance.getThresholdValue());
    }

    @Override
    public AlertRecordVO getAlertDetail(Long alertId) {
        try {
            AlertInstance alertInstance = alertInstanceMapper.selectOneById(alertId);
            if (alertInstance == null) {
                throw new BizException("告警不存在，ID: " + alertId);
            }

            return convertToVO(alertInstance);
        } catch (Exception e) {
            log.error("获取告警详情失败", e);
            throw new BizException("获取告警详情失败", e);
        }
    }

    /**
     * 获取指标显示名称
     */
    private String getMetricDisplayName(String metric) {
        switch (metric) {
            case "startup_time": return "启动时间";
            case "crash_rate": return "崩溃率";
            case "fps": return "FPS";
            case "memory_usage": return "内存使用率";
            case "cpu_usage": return "CPU使用率";
            case "network_success_rate": return "网络请求成功率";
            default: return metric;
        }
    }

    /**
     * 获取严重程度显示名称
     */
    private String getSeverityDisplayName(String severity) {
        switch (severity) {
            case "CRITICAL": return "严重";
            case "WARNING": return "警告";
            case "INFO": return "信息";
            default: return severity;
        }
    }

    @Override
    public List<AlertRecordVO> getAlertList(AlertQueryDTO queryDTO) {
        try {
            // 如果appKey为空字符串，直接返回空列表
            if (queryDTO.getAppKey() == null || queryDTO.getAppKey().trim().isEmpty()) {
                log.warn("获取告警列表: appKey为空，返回空列表");
                return new java.util.ArrayList<>();
            }
            
            // 查询告警实例
            List<AlertInstance> alertInstances = alertInstanceMapper.selectList(queryDTO);

            // 转换为VO对象
            if (alertInstances == null || alertInstances.isEmpty()) {
                return new java.util.ArrayList<>();
            }
            return alertInstances.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取告警列表失败(可能是表未初始化)，返回空列表", e);
            // 表不存在或其他异常时返回空列表而非500错误
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public int countAlerts(AlertQueryDTO queryDTO) {
        try {
            if (queryDTO.getAppKey() == null || queryDTO.getAppKey().trim().isEmpty()) {
                return 0;
            }
            return alertInstanceMapper.selectCount(queryDTO);
        } catch (Exception e) {
            log.error("统计告警数量失败", e);
            return 0;
        }
    }

    /**
     * 转换为VO对象
     */
    private AlertRecordVO convertToVO(AlertInstance alertInstance) {
        AlertRecordVO vo = new AlertRecordVO();
        vo.setId(alertInstance.getId());
        vo.setAppKey(alertInstance.getAppKey());
        vo.setRuleName(alertInstance.getRuleName());
        vo.setMessage(alertInstance.getMessage());
        vo.setSeverity(alertInstance.getSeverity());
        vo.setStatus(alertInstance.getStatus().name());
        vo.setStartTime(alertInstance.getStartTime());
        vo.setEndTime(alertInstance.getEndTime());
        vo.setAssignee(alertInstance.getAssignee());
        vo.setProcessNote(alertInstance.getProcessNote());
        vo.setProcessTime(alertInstance.getProcessTime());
        vo.setCreateTime(alertInstance.getCreateTime());
        vo.setUpdateTime(alertInstance.getUpdateTime());

        return vo;
    }
}
