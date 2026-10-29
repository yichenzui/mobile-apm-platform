package com.monitor.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.monitor.entity.enums.NetworkType;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 性能指标汇总实体类
 */
@Data
@Table("performance_summary")
public class PerformanceSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 汇总ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 应用标识
     */
    @Column("app_key")
    private String appKey;

    /**
     * 日期
     */
    @Column("date")
    private LocalDate date;

    /**
     * 小时（0-23），为空表示日汇总
     */
    @Column("hour")
    private Integer hour;

    /**
     * 应用版本
     */
    @Column("app_version")
    private String appVersion;

    /**
     * 系统版本
     */
    @Column("os_version")
    private String osVersion;

    /**
     * 国家
     */
    @Column("country")
    private String country;

    /**
     * 省份
     */
    @Column("province")
    private String province;

    /**
     * 城市
     */
    @Column("city")
    private String city;

    /**
     * 设备型号
     */
    @Column("device_model")
    private String deviceModel;

    /**
     * 网络类型
     */
    @Column("network_type")
    private String networkType;

    /**
     * 获取网络类型枚举
     * @return 网络类型枚举
     */
    public NetworkType getNetworkTypeEnum() {
        return NetworkType.fromCode(networkType);
    }

    /**
     * 设置网络类型
     * @param networkTypeEnum 网络类型枚举
     */
    public void setNetworkTypeEnum(NetworkType networkTypeEnum) {
        this.networkType = networkTypeEnum.getCode();
    }

    /**
     * 活跃用户数
     */
    @Column("active_users")
    private Integer activeUsers;

    /**
     * 新增用户数
     */
    @Column("new_users")
    private Integer newUsers;

    /**
     * 会话数
     */
    @Column("session_count")
    private Integer sessionCount;

    /**
     * 崩溃次数
     */
    @Column("crash_count")
    private Integer crashCount;

    /**
     * 崩溃率（%）
     */
    @Column("crash_rate")
    private BigDecimal crashRate;

    /**
     * ANR次数
     */
    @Column("anr_count")
    private Integer anrCount;

    /**
     * ANR率（%）
     */
    @Column("anr_rate")
    private BigDecimal anrRate;

    /**
     * 平均启动时间（毫秒）
     */
    @Column("avg_startup_time")
    private BigDecimal avgStartupTime;

    /**
     * 平均CPU使用率（%）
     */
    @Column("avg_cpu_usage")
    private BigDecimal avgCpuUsage;

    /**
     * 平均内存使用量（MB）
     */
    @Column("avg_memory_usage")
    private BigDecimal avgMemoryUsage;

    /**
     * 网络请求次数
     */
    @Column("network_request_count")
    private Integer networkRequestCount;

    /**
     * 网络失败次数
     */
    @Column("network_failure_count")
    private Integer networkFailureCount;

    /**
     * 网络失败率（%）
     */
    @Column("network_failure_rate")
    private BigDecimal networkFailureRate;

    /**
     * 平均网络响应时间（毫秒）
     */
    @Column("avg_network_response_time")
    private BigDecimal avgNetworkResponseTime;

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
}
