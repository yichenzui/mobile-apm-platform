package com.monitor.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 问题VO
 */
@Data
public class IssueVO {

    /**
     * 问题ID
     */
    private Long id;

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题类型
     */
    private String type;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 严重程度：1-低，2-中，3-高，4-严重
     */
    private Integer severity;

    /**
     * 状态：0-待处理，1-处理中，2-已解决，3-已忽略
     */
    private Integer status;

    /**
     * 发生次数
     */
    private Integer occurrenceCount;

    /**
     * 影响用户数
     */
    private Integer affectedUserCount;

    /**
     * 首次发生时间
     */
    private LocalDateTime firstOccurrenceTime;

    /**
     * 最后发生时间
     */
    private LocalDateTime lastOccurrenceTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 堆栈信息
     */
    private String stackTrace;

    /**
     * 设备信息
     */
    private String deviceInfo;

    /**
     * 处理人
     */
    private String assignee;

    /**
     * 处理说明
     */
    private String processNote;
}
