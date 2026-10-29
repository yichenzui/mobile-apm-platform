package com.monitor.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 告警记录VO
 */
@Data
public class AlertRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 规则ID
     */
    private Long ruleId;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 监控指标
     */
    private String metric;

    /**
     * 监控指标名称
     */
    private String metricName;

    /**
     * 实际值
     */
    private BigDecimal value;

    /**
     * 阈值
     */
    private BigDecimal threshold;

    /**
     * 条件：GT（大于），GTE（大于等于），LT（小于），LTE（小于等于）
     */
    private String condition;

    /**
     * 严重程度
     */
    private String severity;

    /**
     * 严重程度名称
     */
    private String severityName;

    /**
     * 状态：FIRING（告警中），RESOLVED（已恢复）
     */
    private String status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 告警开始时间
     */
    private LocalDateTime startTime;

    /**
     * 告警结束时间
     */
    private LocalDateTime endTime;

    /**
     * 告警持续时间（分钟）
     */
    private Long duration;

    /**
     * 告警消息
     */
    private String message;

    /**
     * 是否已发送通知：0-未发送，1-已发送
     */
    private Integer notificationSent;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 处理人
     */
    private String assignee;

    /**
     * 处理说明
     */
    private String processNote;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 当前值
     */
    private String currentValue;

    /**
     * 阈值
     */
    private String thresholdValue;
}
