package com.monitor.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 看板汇总数据VO
 */
@Data
public class DashboardSummaryVO {

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 实时在线用户数
     */
    private Integer onlineUsers;

    /**
     * 今日崩溃率（%）
     */
    private BigDecimal crashRate;

    /**
     * 平均启动时间
     */
    private StartupTimeStats startupTime;

    /**
     * 卡顿率（%）
     */
    private BigDecimal lagRate;

    /**
     * FPS分布
     */
    private List<FpsDistribution> fpsDistribution;

    /**
     * 网络请求成功率（%）
     */
    private BigDecimal networkSuccessRate;

    /**
     * 地理分布（前10个省份）
     */
    private List<RegionDistribution> regionDistribution;

    /**
     * 版本分布
     */
    private List<VersionDistribution> versionDistribution;

    /**
     * 趋势数据
     */
    private List<TrendData> trendData;

    /**
     * 启动时间统计
     */
    @Data
    public static class StartupTimeStats {
        /**
         * P50启动时间（毫秒）
         */
        private BigDecimal p50;

        /**
         * P90启动时间（毫秒）
         */
        private BigDecimal p90;

        /**
         * P99启动时间（毫秒）
         */
        private BigDecimal p99;
    }

    /**
     * FPS分布
     */
    @Data
    public static class FpsDistribution {
        /**
         * FPS范围（如：0-20, 20-40, 40-60）
         */
        private String range;

        /**
         * 用户数
         */
        private Integer userCount;

        /**
         * 占比（%）
         */
        private BigDecimal percentage;
    }

    /**
     * 地理分布
     */
    @Data
    public static class RegionDistribution {
        /**
         * 省份名称
         */
        private String province;

        /**
         * 用户数
         */
        private Integer userCount;

        /**
         * 占比（%）
         */
        private BigDecimal percentage;

        /**
         * 崩溃率（%）
         */
        private BigDecimal crashRate;
    }

    /**
     * 版本分布
     */
    @Data
    public static class VersionDistribution {
        /**
         * 版本号
         */
        private String version;

        /**
         * 用户数
         */
        private Integer userCount;

        /**
         * 崩溃率（%）
         */
        private BigDecimal crashRate;

        /**
         * 占比（%）
         */
        private BigDecimal percentage;
    }

    /**
     * 趋势数据
     */
    @Data
    public static class TrendData {
        /**
         * 时间
         */
        private String time;

        /**
         * 崩溃率（%）
         */
        private BigDecimal crashRate;

        /**
         * 启动时间（毫秒）
         */
        private BigDecimal startupTime;

        /**
         * 卡顿率（%）
         */
        private BigDecimal lagRate;
    }
}
