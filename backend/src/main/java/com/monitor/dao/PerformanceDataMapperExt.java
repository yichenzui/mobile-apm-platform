package com.monitor.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 性能数据 Mapper 扩展接口
 */
@Mapper
public interface PerformanceDataMapperExt {

    /**
     * 查询启动时间列表
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 启动时间列表
     */
    List<Map<String, Object>> selectStartupTimes(
        @Param("appKey") String appKey, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询FPS分布
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return FPS分布列表
     */
    List<Map<String, Object>> selectFpsDistribution(
        @Param("appKey") String appKey, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按设备维度聚合（每个设备的事件数、崩溃/ANR/卡顿次数、平均启动时间）
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 设备维度列表
     */
    List<Map<String, Object>> selectDeviceDistribution(
        @Param("appKey") String appKey,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按网络类型维度聚合
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 网络维度列表
     */
    List<Map<String, Object>> selectNetworkDistribution(
        @Param("appKey") String appKey,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
