package com.monitor.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.monitor.entity.enums.AlertSeverity;

import lombok.Data;

/**
 * 告警规则实体类
 */
@Data
@Table("alert_rule")
public class AlertRule implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规则ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 应用标识
     */
    @Column("app_key")
    private String appKey;

    /**
     * 规则名称
     */
    @Column("name")
    private String name;

    /**
     * 规则名称
     */
    @Column("rule_name")
    private String ruleName;


    /**
     * 规则类型
     */
    @Column("rule_type")
    private RuleType ruleType;


    /**
     * 监控指标：CRASH_RATE，ANR_RATE，STARTUP_TIME，CPU_USAGE，MEMORY_USAGE
     */
    @Column("metric")
    private String metric;

    /**
     * 条件：GT（大于），GTE（大于等于），LT（小于），LTE（小于等于）
     */
    @Column("condition")
    private String condition;

        /**
     * 条件配置JSON
     */
    @Column("condition_config")
    private String conditionConfig;


    /**
     * 阈值
     */
    @Column("threshold")
    private BigDecimal threshold;

    /**
     * 持续时间（分钟）
     */
    @Column("duration")
    private Integer duration;

    /**
     * 严重程度：LOW，MEDIUM，HIGH，CRITICAL
     */
    @Column("severity")
    private String severity;

    /**
     * 获取严重程度枚举
     * @return 告警严重程度枚举
     */
    public AlertSeverity getSeverityEnum() {
        return AlertSeverity.fromCode(severity);
    }

    /**
     * 设置严重程度
     * @param severityEnum 严重程度枚举
     */
    public void setSeverityEnum(AlertSeverity severityEnum) {
        this.severity = severityEnum.getCode();
    }

    /**
     * 状态：0-禁用，1-启用
     */
    @Column("status")
    private Integer status;

        /**
     * 是否启用：0-禁用，1-启用
     */
    @Column("enabled")
    private Integer enabled;


    /**
     * 通知渠道：EMAIL，WEBHOOK，SMS
     */
    @Column("notification_channels")
    private String notificationChannels;

    /**
     * 通知配置JSON
     */
    @Column("notification_config")
    private String notificationConfig;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;

     /**
     * 规则类型枚举
     */
    public enum RuleType {
        THRESHOLD,    // 阈值规则
        TREND,        // 趋势规则
        YOY,          // 同比规则
        MOM,          // 环比规则
        ANOMALY       // 异常检测规则
    }

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column("deleted")
    private Boolean deleted;
}