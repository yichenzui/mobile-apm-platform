package com.monitor.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.monitor.entity.PerformanceSummary;
import com.mybatisflex.core.BaseMapper;

/**
 * 性能汇总 Mapper 接口
 */
@Mapper
public interface PerformanceSummaryMapper extends BaseMapper<PerformanceSummary> {

    /**
     * 按 appKey 删除所有演示数据
     */
    @Delete("DELETE FROM performance_summary WHERE app_key = #{appKey}")
    int deleteByAppKey(@Param("appKey") String appKey);

    /**
     * 查询崩溃率统计
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param granularity 粒度
     * @return 崩溃率统计结果
     */
    Map<String, Object> selectCrashStats(
        @Param("appKey") String appKey, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime, 
        @Param("granularity") String granularity
    );

    /**
     * 查询卡顿率统计
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param granularity 粒度
     * @return 卡顿率统计结果
     */
    Map<String, Object> selectLagStats(
        @Param("appKey") String appKey, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime, 
        @Param("granularity") String granularity
    );

    /**
     * 查询网络统计
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param granularity 粒度
     * @return 网络统计结果
     */
    Map<String, Object> selectNetworkStats(
        @Param("appKey") String appKey, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime, 
        @Param("granularity") String granularity
    );

    /**
     * 查询地区分布
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 限制数量
     * @return 地区分布列表
     */
    List<Map<String, Object>> selectRegionDistribution(
        @Param("appKey") String appKey, 
        @Param("startTime") LocalDateTime startTime, 
        @Param("endTime") LocalDateTime endTime,
        @Param("limit") Integer limit
    );

    /**
     * 查询趋势数据（按日期分组）
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 趋势数据列表（每行含date, crashRate, startupTime, lagRate）
     */
    List<Map<String, Object>> selectTrendData(
        @Param("appKey") String appKey,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 查询版本分布
     *
     * @param appKey 应用Key
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 版本分布列表（每行含version, userCount, crashRate）
     */
    List<Map<String, Object>> selectVersionDistribution(
        @Param("appKey") String appKey,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
