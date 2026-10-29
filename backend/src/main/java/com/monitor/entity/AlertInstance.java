package com.monitor.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;

import lombok.Data;

/**
 * 告警实例实体类
 */
@Data
@Table("alert_instance")
public class AlertInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 规则ID
     */
    @Column("rule_id")
    private Long ruleId;

    /**
     * 规则名称
     */
    @Column("rule_name")
    private String ruleName;

    /**
     * 应用标识
     */
    @Column("app_key")
    private String appKey;

    /**
     * 指标名称
     */
    @Column("metric")
    private String metric;

    /**
     * 严重程度
     */
    @Column("severity")
    private String severity;

    /**
     * 当前值
     */
    @Column("current_value")
    private BigDecimal currentValue;

    /**
     * 阈值
     */
    @Column("threshold_value")
    private String thresholdValue;

    /**
     * 告警状态
     */
    @Column("status")
    private AlertStatus status;

    /**
     * 开始时间
     */
    @Column("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column("end_time")
    private LocalDateTime endTime;

    /**
     * 指标时间
     */
    @Column("metric_time")
    private LocalDateTime metricTime;

    /**
     * 告警消息
     */
    @Column("message")
    private String message;

    /**
     * 处理人
     */
    @Column("assignee")
    private String assignee;

    /**
     * 处理时间
     */
    @Column("process_time")
    private LocalDateTime processTime;

    /**
     * 处理备注
     */
    @Column("process_note")
    private String processNote;

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
     * 告警状态枚举
     */
    public enum AlertStatus {
        /**
         * 告警中
         */
        FIRING("FIRING", "告警中"),

        /**
         * 已恢复
         */
        RESOLVED("RESOLVED", "已恢复"),

        /**
         * 已确认
         */
        ACKNOWLEDGED("ACKNOWLEDGED", "已确认"),

        /**
         * 已关闭
         */
        CLOSED("CLOSED", "已关闭");

        private final String code;
        private final String description;

        AlertStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        /**
         * 根据代码获取枚举
         */
        public static AlertStatus fromCode(String code) {
            for (AlertStatus status : values()) {
                if (status.getCode().equals(code)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown alert status code: " + code);
        }
        
    }

    /**
     * 通知渠道配置JSON
     */
    @Column("notify_channels")
    private String notifyChannels;

    public String getNotifyChannels() {
        return notifyChannels;
    }

    public void setNotifyChannels(String notifyChannels) {
        this.notifyChannels = notifyChannels;
    }
}
