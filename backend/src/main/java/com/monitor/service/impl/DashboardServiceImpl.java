package com.monitor.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.monitor.dao.ApplicationMapper;
import com.monitor.dao.PerformanceDataMapperExt;
import com.monitor.dao.PerformanceSummaryMapper;
import com.monitor.dto.DashboardQueryDTO;
import com.monitor.entity.Application;
import com.monitor.service.DashboardService;
import com.monitor.vo.DashboardSummaryVO;

import lombok.extern.slf4j.Slf4j;

/**
 * 看板服务实现类
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    // log 由 @Slf4j 自动生成，此处不再手写 Logger 字段（原写法与 Lombok 生成的 log 重复）
    private final PerformanceSummaryMapper performanceSummaryMapper;
    private final ApplicationMapper applicationMapper;
    private final PerformanceDataMapperExt performanceDataMapperExt;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public DashboardServiceImpl(PerformanceSummaryMapper performanceSummaryMapper,
                                ApplicationMapper applicationMapper,
                                PerformanceDataMapperExt performanceDataMapperExt) {
        this.performanceSummaryMapper = performanceSummaryMapper;
        this.applicationMapper = applicationMapper;
        this.performanceDataMapperExt = performanceDataMapperExt;
    }

    private static final String DASHBOARD_CACHE_PREFIX = "dashboard:summary:";
    private static final int CACHE_EXPIRE_MINUTES = 5;

    /**
     * 安全获取Map中的字符串值，避免NPE
     */
    private static String safeString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    /**
     * 安全获取Map中的字符串值并转为int，避免NPE
     */
    private static int safeInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public DashboardSummaryVO getDashboardSummary(DashboardQueryDTO queryDTO) {
        // 如果有Redis，尝试从缓存获取
        if (redisTemplate != null) {
            String cacheKey = buildCacheKey(queryDTO);
            try {
                DashboardSummaryVO cachedData = (DashboardSummaryVO) redisTemplate.opsForValue().get(cacheKey);
                if (cachedData != null) {
                    log.debug("从缓存获取看板数据，应用：{}", queryDTO.getAppKey());
                    return cachedData;
                }
            } catch (Exception e) {
                log.warn("Redis缓存读取失败，继续查数据库: {}", e.getMessage());
            }
        }

        // 从数据库获取数据
        DashboardSummaryVO summary = buildDashboardSummary(queryDTO);

        // 如果有Redis，存入缓存
        if (redisTemplate != null) {
            try {
                String cacheKey = buildCacheKey(queryDTO);
                redisTemplate.opsForValue().set(cacheKey, summary, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            } catch (Exception e) {
                log.warn("Redis缓存写入失败，忽略: {}", e.getMessage());
            }
        }

        return summary;
    }

    /**
     * 构建缓存键
     */
    private String buildCacheKey(DashboardQueryDTO queryDTO) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        return DASHBOARD_CACHE_PREFIX + queryDTO.getAppKey() + ":" + 
               queryDTO.getStartTime().format(formatter) + ":" + 
               queryDTO.getEndTime().format(formatter) + ":" + 
               queryDTO.getGranularity();
    }

    /**
     * 构建看板汇总数据
     */
    private DashboardSummaryVO buildDashboardSummary(DashboardQueryDTO queryDTO) {
        DashboardSummaryVO summary = new DashboardSummaryVO();
        summary.setAppKey(queryDTO.getAppKey());

        // 获取应用信息
        Application app = applicationMapper.selectOneByAppKey(queryDTO.getAppKey());
        if (app != null) {
            summary.setAppName(app.getName());
        }

        // 获取实时在线用户数
        summary.setOnlineUsers(getOnlineUsers(queryDTO.getAppKey()));

        // 获取今日崩溃率
        summary.setCrashRate(getCrashRate(queryDTO));

        // 获取平均启动时间（P50, P90, P99）
        summary.setStartupTime(getStartupTimeStats(queryDTO));

        // 获取卡顿率
        summary.setLagRate(getLagRate(queryDTO));

        // 获取FPS分布
        summary.setFpsDistribution(getFpsDistribution(queryDTO));

        // 获取网络请求成功率
        summary.setNetworkSuccessRate(getNetworkSuccessRate(queryDTO));

        // 获取地理分布（前10个省份）
        summary.setRegionDistribution(getRegionDistribution(queryDTO));

        // 添加趋势数据
        summary.setTrendData(getTrendData(queryDTO));

        // 添加版本分布数据
        summary.setVersionDistribution(getVersionDistribution(queryDTO));

        return summary;
    }

    /**
     * 获取实时在线用户数
     */
    private Integer getOnlineUsers(String appKey) {
        if (redisTemplate == null) {
            return 0;
        }
        try {
            String key = "online_users:" + appKey;
            Object count = redisTemplate.opsForValue().get(key);
            return count != null ? Integer.parseInt(count.toString()) : 0;
        } catch (Exception e) {
            log.warn("获取在线用户数失败: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 获取崩溃率
     */
    private BigDecimal getCrashRate(DashboardQueryDTO queryDTO) {
        // 从汇总表获取数据
        Map<String, Object> result = performanceSummaryMapper.selectCrashStats(
            queryDTO.getAppKey(), 
            queryDTO.getStartTime(), 
            queryDTO.getEndTime(), 
            queryDTO.getGranularity()
        );
        if (result == null || result.get("crashRate") == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(result.get("crashRate").toString());
    }

    /**
     * 获取启动时间统计
     */
    private DashboardSummaryVO.StartupTimeStats getStartupTimeStats(DashboardQueryDTO queryDTO) {
        List<Map<String, Object>> startupTimes = performanceDataMapperExt.selectStartupTimes(
            queryDTO.getAppKey(), 
            queryDTO.getStartTime(), 
            queryDTO.getEndTime()
        );
        if (startupTimes == null || startupTimes.isEmpty()) {
            return new DashboardSummaryVO.StartupTimeStats();
        }

        // 计算分位值
        List<Integer> times = startupTimes.stream()
            .map(item -> safeInt(item, "startupTime"))
            .sorted()
            .collect(Collectors.toList());

        DashboardSummaryVO.StartupTimeStats stats = new DashboardSummaryVO.StartupTimeStats();
        stats.setP50(calculatePercentile(times, 50));
        stats.setP90(calculatePercentile(times, 90));
        stats.setP99(calculatePercentile(times, 99));

        return stats;
    }

    /**
     * 获取卡顿率
     */
    private BigDecimal getLagRate(DashboardQueryDTO queryDTO) {
        Map<String, Object> result = performanceSummaryMapper.selectLagStats(
            queryDTO.getAppKey(), 
            queryDTO.getStartTime(), 
            queryDTO.getEndTime(), 
            queryDTO.getGranularity()
        );
        if (result == null || result.get("lagRate") == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(result.get("lagRate").toString());
    }

    /**
     * 获取FPS分布
     */
    private List<DashboardSummaryVO.FpsDistribution> getFpsDistribution(DashboardQueryDTO queryDTO) {
        List<Map<String, Object>> fpsData = performanceDataMapperExt.selectFpsDistribution(
            queryDTO.getAppKey(), 
            queryDTO.getStartTime(), 
            queryDTO.getEndTime()
        );
        if (fpsData == null || fpsData.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算总数
        int totalCount = fpsData.stream()
            .mapToInt(item -> safeInt(item, "userCount"))
            .sum();

        // 转换为分布数据
        return fpsData.stream()
            .map(item -> {
                DashboardSummaryVO.FpsDistribution dist = new DashboardSummaryVO.FpsDistribution();
                dist.setRange(safeString(item, "fpsRange"));
                dist.setUserCount(safeInt(item, "userCount"));

                // 计算百分比
                int count = dist.getUserCount();
                BigDecimal percentage = totalCount > 0 
                    ? BigDecimal.valueOf(count * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                dist.setPercentage(percentage);

                return dist;
            })
            .collect(Collectors.toList());
    }

    /**
     * 获取网络请求成功率
     */
    private BigDecimal getNetworkSuccessRate(DashboardQueryDTO queryDTO) {
        Map<String, Object> result = performanceSummaryMapper.selectNetworkStats(
            queryDTO.getAppKey(), 
            queryDTO.getStartTime(), 
            queryDTO.getEndTime(), 
            queryDTO.getGranularity()
        );
        if (result == null || result.get("successRate") == null) {
            return BigDecimal.ZERO;
        }

        return new BigDecimal(result.get("successRate").toString());
    }

    /**
     * 获取地理分布（所有省份）
     */
    private List<DashboardSummaryVO.RegionDistribution> getRegionDistribution(DashboardQueryDTO queryDTO) {
        List<Map<String, Object>> regionData = performanceSummaryMapper.selectRegionDistribution(
            queryDTO.getAppKey(), 
            queryDTO.getStartTime(), 
            queryDTO.getEndTime(),
            null
        );
        if (regionData == null || regionData.isEmpty()) {
            return new ArrayList<>();
        }

        // 计算总数
        int totalCount = regionData.stream()
            .mapToInt(item -> safeInt(item, "userCount"))
            .sum();

        // 转换为分布数据
        return regionData.stream()
            .map(item -> {
                DashboardSummaryVO.RegionDistribution dist = new DashboardSummaryVO.RegionDistribution();
                dist.setProvince(safeString(item, "province"));
                dist.setUserCount(safeInt(item, "userCount"));

                // 计算百分比
                int count = dist.getUserCount();
                BigDecimal percentage = totalCount > 0 
                    ? BigDecimal.valueOf(count * 100.0 / totalCount).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                dist.setPercentage(percentage);

                // 获取崩溃率
                if (item.get("crashRate") != null) {
                    dist.setCrashRate(new BigDecimal(item.get("crashRate").toString()));
                } else {
                    dist.setCrashRate(BigDecimal.ZERO);
                }

                return dist;
            })
            .collect(Collectors.toList());
    }

    /**
     * 计算分位值
     */
    private BigDecimal calculatePercentile(List<Integer> sortedList, int percentile) {
        if (sortedList == null || sortedList.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int index = (int) Math.ceil(percentile / 100.0 * sortedList.size()) - 1;
        index = Math.max(0, Math.min(index, sortedList.size() - 1));

        return BigDecimal.valueOf(sortedList.get(index));
    }
    
    /**
     * 趋势数据（按日期分组的性能指标趋势）
     */
    private List<DashboardSummaryVO.TrendData> getTrendData(DashboardQueryDTO queryDTO) {
        List<Map<String, Object>> trendRows = performanceSummaryMapper.selectTrendData(
            queryDTO.getAppKey(),
            queryDTO.getStartTime(),
            queryDTO.getEndTime()
        );
        if (trendRows == null || trendRows.isEmpty()) {
            return new ArrayList<>();
        }
        return trendRows.stream().map(row -> {
            DashboardSummaryVO.TrendData td = new DashboardSummaryVO.TrendData();
            td.setTime(safeString(row, "time"));
            if (row.get("crashRate") != null) {
                td.setCrashRate(new BigDecimal(row.get("crashRate").toString()).setScale(3, RoundingMode.HALF_UP));
            } else {
                td.setCrashRate(BigDecimal.ZERO);
            }
            // 启动时间从汇总表无法获取，设为0（前端可从KPI卡片获取）
            td.setStartupTime(BigDecimal.ZERO);
            if (row.get("lagRate") != null) {
                td.setLagRate(new BigDecimal(row.get("lagRate").toString()).setScale(3, RoundingMode.HALF_UP));
            } else {
                td.setLagRate(BigDecimal.ZERO);
            }
            return td;
        }).collect(Collectors.toList());
    }

    /**
     * 版本分布
     */
    private List<DashboardSummaryVO.VersionDistribution> getVersionDistribution(DashboardQueryDTO queryDTO) {
        List<Map<String, Object>> versionRows = performanceSummaryMapper.selectVersionDistribution(
            queryDTO.getAppKey(),
            queryDTO.getStartTime(),
            queryDTO.getEndTime()
        );
        if (versionRows == null || versionRows.isEmpty()) {
            return new ArrayList<>();
        }
        int totalUsers = versionRows.stream()
            .mapToInt(row -> safeInt(row, "userCount"))
            .sum();
        return versionRows.stream().map(row -> {
            DashboardSummaryVO.VersionDistribution vd = new DashboardSummaryVO.VersionDistribution();
            vd.setVersion(safeString(row, "version"));
            vd.setUserCount(safeInt(row, "userCount"));
            int count = vd.getUserCount();
            vd.setPercentage(totalUsers > 0
                ? BigDecimal.valueOf(count * 100.0 / totalUsers).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO);
            if (row.get("crashRate") != null) {
                vd.setCrashRate(new BigDecimal(row.get("crashRate").toString()).setScale(3, RoundingMode.HALF_UP));
            } else {
                vd.setCrashRate(BigDecimal.ZERO);
            }
            return vd;
        }).collect(Collectors.toList());
    }
}
