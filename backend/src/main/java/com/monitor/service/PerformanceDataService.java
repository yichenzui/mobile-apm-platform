package com.monitor.service;

import java.time.LocalDate;

import com.monitor.dto.PerformanceDataReportDTO;

/**
 * 性能数据服务接口
 */
public interface PerformanceDataService {

    /**
     * 批量保存性能数据
     *
     * @param reportDTO 性能数据上报DTO
     */
    void batchSavePerformanceData(PerformanceDataReportDTO reportDTO);

    /**
     * 重算指定应用某天的汇总数据（幂等）
     *
     * 用于定时聚合任务：上报时是增量累加，定时任务基于原始明细重算以保证最终准确。
     *
     * @param appKey 应用标识
     * @param date   日期
     */
    void recomputeDailySummary(String appKey, LocalDate date);

    Object getPerformanceTrend(com.monitor.dto.DashboardQueryDTO queryDTO);

    Object getGeoDistribution(com.monitor.dto.DashboardQueryDTO queryDTO);

    Object getVersionComparison(com.monitor.dto.DashboardQueryDTO queryDTO);

    Object getFpsDistribution(com.monitor.dto.DashboardQueryDTO queryDTO);

    /**
     * 按设备维度聚合
     */
    Object getDeviceDistribution(com.monitor.dto.DashboardQueryDTO queryDTO);

    /**
     * 按网络类型维度聚合
     */
    Object getNetworkDistribution(com.monitor.dto.DashboardQueryDTO queryDTO);
}
