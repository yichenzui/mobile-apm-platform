package com.monitor.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.monitor.entity.enums.IssueSeverity;
import com.monitor.entity.enums.IssueStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 问题跟踪实体类
 */
@Data
@Table("issue")
public class Issue implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 问题ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 应用标识
     */
    @Column("app_key")
    private String appKey;

    /**
     * 问题标题
     */
    @Column("title")
    private String title;

    /**
     * 问题描述
     */
    @Column("description")
    private String description;

    /**
     * 问题类型：CRASH，ANR，PERFORMANCE，UI
     */
    @Column("type")
    private String type;

    /**
     * 严重程度：LOW，MEDIUM，HIGH，CRITICAL
     */
    @Column("severity")
    private Integer severity;

    /**
     * 获取严重程度枚举
     * @return 问题严重程度枚举
     */
    public IssueSeverity getSeverityEnum() {
        return IssueSeverity.fromCode(severity);
    }

    /**
     * 设置严重程度
     * @param severityEnum 严重程度枚举
     */
    public void setSeverityEnum(IssueSeverity severityEnum) {
        this.severity = severityEnum.getCode();
    }

    /**
     * 状态：0-待处理，1-处理中，2-已解决，3-已忽略
     */
    @Column("status")
    private Integer status;

    /**
     * 获取状态枚举
     * @return 问题状态枚举
     */
    public IssueStatus getStatusEnum() {
        return IssueStatus.fromCode(status);
    }

    /**
     * 设置状态
     * @param statusEnum 状态枚举
     */
    public void setStatusEnum(IssueStatus statusEnum) {
        this.status = statusEnum.getCode();
    }

    /**
     * 指派人ID
     */
    @Column("assignee_id")
    private Long assigneeId;

    /**
     * 报告人ID
     */
    @Column("reporter_id")
    private Long reporterId;

    /**
     * 堆栈跟踪
     */
    @Column("stack_trace")
    private String stackTrace;

    /**
     * 复现步骤
     */
    @Column("reproduce_steps")
    private String reproduceSteps;

    /**
     * 预期结果
     */
    @Column("expected_result")
    private String expectedResult;

    /**
     * 实际结果
     */
    @Column("actual_result")
    private String actualResult;

    /**
     * 附件列表JSON
     */
    @Column("attachments")
    private String attachments;

    /**
     * 设备信息
     */
    @Column("device_info")
    private String deviceInfo;

    /**
     * 发生次数
     */
    @Column("occurrence_count")
    private Integer occurrenceCount;

    /**
     * 影响用户数
     */
    @Column("affected_user_count")
    private Integer affectedUserCount;

    /**
     * 首次发生时间
     */
    @Column("first_occurrence_time")
    private LocalDateTime firstOccurrenceTime;

    /**
     * 最后发生时间
     */
    @Column("last_occurrence_time")
    private LocalDateTime lastOccurrenceTime;

    /**
     * 处理人
     */
    @Column("assignee")
    private String assignee;

    /**
     * 处理说明
     */
    @Column("process_note")
    private String processNote;

    /**
     * 处理时间
     */
    @Column("process_time")
    private LocalDateTime processTime;

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
     * 解决时间
     */
    @Column("resolved_time")
    private LocalDateTime resolvedTime;

    /**
     * 是否删除：false-未删除，true-已删除
     */
    @Column("deleted")
    private Boolean deleted;
}
