package com.monitor.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.monitor.handler.JsonNodeTypeHandler;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 性能数据实体类
 */
@Data
@Table("performance_data")
public class PerformanceDataNew implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 应用标识
     */
    @Column("app_key")
    private String appKey;

    /**
     * 设备ID
     */
    @Column("device_id")
    private String deviceId;

    /**
     * 会话ID
     */
    @Column("session_id")
    private String sessionId;

    /**
     * 事件类型：startup/crash/anr/fps/memory/network
     */
    @Column("event_type")
    private String eventType;

    /**
     * 事件详情（JSON格式）
     */
    @Column(value = "event_data", typeHandler = JsonNodeTypeHandler.class)
    private JsonNode eventData;

    /**
     * 事件发生时间
     */
    @Column("event_time")
    private LocalDateTime eventTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 应用版本
     */
    @Column("version_code")
    private String versionCode;

    /**
     * 系统版本
     */
    @Column("os_version")
    private String osVersion;

    /**
     * 网络类型
     */
    @Column("network_type")
    private String networkType;

    /**
     * 省份（取自上报 extras.province，用于地域维度聚合）
     */
    @Column("province")
    private String province;
}
