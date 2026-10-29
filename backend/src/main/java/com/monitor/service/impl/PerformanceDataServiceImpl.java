package com.monitor.service.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.monitor.dao.AlertRuleMapper;
import com.monitor.dao.PerformanceDataMapperExt;
import com.monitor.dao.PerformanceSummaryMapper;
import com.monitor.dto.DashboardQueryDTO;
import com.monitor.dto.MetricDataDTO;
import com.monitor.dto.PerformanceDataReportDTO;
import com.monitor.entity.AlertRule;
import com.monitor.entity.PerformanceDataNew;
import com.monitor.service.AlertRuleEngine;
import com.monitor.service.PerformanceDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 性能数据服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceDataServiceImpl implements PerformanceDataService {

    private final PerformanceSummaryMapper performanceSummaryMapper;
    private final PerformanceDataMapperExt performanceDataMapperExt;

    /** 直接操作聚合表：performance_summary 缺少 lag_count 等实体字段，用 SQL 更灵活 */
    private final JdbcTemplate jdbcTemplate;
    private final AlertRuleMapper alertRuleMapper;
    private final AlertRuleEngine alertRuleEngine;

    @Override
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void batchSavePerformanceData(PerformanceDataReportDTO reportDTO) {
        String appKey = reportDTO.getAppKey();
        List<PerformanceDataNew> dataList = convertToEntityList(reportDTO);

        // 批量插入性能数据
        // 说明：改用 JdbcTemplate 批量插入，原因是 MyBatis-Flex 的 insertBatch 未包含
        // 后加的 province 列，会导致地域维度无法从明细表重算。
        batchInsertRawData(dataList);

        // 异步处理数据聚合
        processDataAggregation(appKey, dataList);

        // 异步检测告警
        checkAlerts(appKey, dataList);

        log.info("成功保存性能数据，应用：{}，数据条数：{}", appKey, dataList.size());
    }

    @Override
    public Object getPerformanceTrend(DashboardQueryDTO queryDTO) {
        try {
            List<Map<String, Object>> trendRows = performanceSummaryMapper.selectTrendData(
                queryDTO.getAppKey(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
            );
            if (trendRows == null || trendRows.isEmpty()) {
                log.debug("趋势数据为空，appKey={}", queryDTO.getAppKey());
                return new ArrayList<>();
            }
            return trendRows.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                double avgStartupTime = safeDouble(row, "avgStartupTime", 0);
                item.put("time", row.get("time") != null ? row.get("time").toString() : "");
                item.put("crashRate", safeDouble(row, "crashRate", 3));
                item.put("startupTime", avgStartupTime);   // 兼容前端指标名
                item.put("avgStartupTime", avgStartupTime);
                item.put("lagRate", safeDouble(row, "lagRate", 3));
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取趋势数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Object getGeoDistribution(DashboardQueryDTO queryDTO) {
        try {
            // 不传limit或设为null以获取全部省份数据（地图需要展示所有省份）
            List<Map<String, Object>> regionRows = performanceSummaryMapper.selectRegionDistribution(
                queryDTO.getAppKey(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime(),
                null
            );
            if (regionRows == null || regionRows.isEmpty()) {
                log.debug("地理分布数据为空，appKey={}", queryDTO.getAppKey());
                return new ArrayList<>();
            }
            int totalCount = regionRows.stream()
                .mapToInt(item -> safeInt(item, "userCount"))
                .sum();
            return regionRows.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                item.put("province", row.get("province") != null ? row.get("province").toString() : "未知");
                int userCount = safeInt(row, "userCount");
                item.put("userCount", userCount);
                double percentage = totalCount > 0
                    ? userCount * 100.0 / totalCount : 0.0;
                item.put("percentage", Math.round(percentage * 100.0) / 100.0);
                item.put("crashRate", safeDouble(row, "crashRate", 2));
                item.put("crashCount", safeInt(row, "crashCount"));
                item.put("anrCount", safeInt(row, "anrCount"));
                item.put("avgStartupTime", safeDouble(row, "avgStartupTime", 0));
                item.put("networkSuccessRate", safeDouble(row, "networkSuccessRate", 1));
                item.put("networkFailureRate", safeDouble(row, "networkFailureRate", 2));
                item.put("lagRate", safeDouble(row, "lagRate", 2));
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取地理分布数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Object getVersionComparison(DashboardQueryDTO queryDTO) {
        try {
            List<Map<String, Object>> versionRows = performanceSummaryMapper.selectVersionDistribution(
                queryDTO.getAppKey(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
            );
            if (versionRows == null || versionRows.isEmpty()) {
                log.debug("版本分布数据为空，appKey={}", queryDTO.getAppKey());
                return new ArrayList<>();
            }
            int totalUsers = versionRows.stream()
                .mapToInt(row -> safeInt(row, "userCount"))
                .sum();
            return versionRows.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                double avgStartupTime = safeDouble(row, "avgStartupTime", 0);
                item.put("version", row.get("version") != null ? row.get("version").toString() : "未知");
                int userCount = safeInt(row, "userCount");
                item.put("userCount", userCount);
                item.put("activeUsers", userCount);   // 前端"活跃用户"列统一用 activeUsers
                double percentage = totalUsers > 0
                    ? userCount * 100.0 / totalUsers : 0.0;
                item.put("percentage", Math.round(percentage * 100.0) / 100.0);
                item.put("crashRate", safeDouble(row, "crashRate", 3));
                item.put("crashCount", safeInt(row, "crashCount"));
                item.put("anrCount", safeInt(row, "anrCount"));
                item.put("anrRate", safeDouble(row, "anrRate", 3));
                item.put("startupTime", avgStartupTime);   // 兼容前端版本对比指标名
                item.put("avgStartupTime", avgStartupTime);
                item.put("avgCpuUsage", safeDouble(row, "avgCpuUsage", 1));
                item.put("avgMemoryUsage", safeDouble(row, "avgMemoryUsage", 1));
                item.put("networkFailureRate", safeDouble(row, "networkFailureRate", 2));
                item.put("networkSuccessRate", safeDouble(row, "networkSuccessRate", 1));
                item.put("lagRate", safeDouble(row, "lagRate", 3));
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取版本对比数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Object getFpsDistribution(DashboardQueryDTO queryDTO) {
        try {
            List<Map<String, Object>> fpsRows = performanceDataMapperExt.selectFpsDistribution(
                queryDTO.getAppKey(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
            );
            if (fpsRows == null || fpsRows.isEmpty()) {
                log.debug("FPS分布数据为空，appKey={}", queryDTO.getAppKey());
                return new ArrayList<>();
            }
            int totalCount = fpsRows.stream()
                .mapToInt(item -> Integer.parseInt(item.get("userCount").toString()))
                .sum();
            return fpsRows.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                item.put("range", row.get("fpsRange") != null ? row.get("fpsRange").toString() : "未知");
                item.put("userCount", Integer.parseInt(row.get("userCount").toString()));
                double percentage = totalCount > 0
                    ? Double.parseDouble(row.get("userCount").toString()) * 100.0 / totalCount
                    : 0.0;
                item.put("percentage", Math.round(percentage * 100.0) / 100.0);
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取FPS分布数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Object getDeviceDistribution(DashboardQueryDTO queryDTO) {
        try {
            List<Map<String, Object>> rows = performanceDataMapperExt.selectDeviceDistribution(
                queryDTO.getAppKey(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
            );
            if (rows == null || rows.isEmpty()) {
                log.debug("设备维度数据为空，appKey={}", queryDTO.getAppKey());
                return new ArrayList<>();
            }
            return rows.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                int eventCount = safeInt(row, "eventCount");
                int crashCount = safeInt(row, "crashCount");
                int startCount = safeInt(row, "startCount");
                item.put("deviceId", row.get("deviceId") != null ? row.get("deviceId").toString() : "未知");
                item.put("deviceModel", row.get("deviceId"));   // 前端表格第一列
                item.put("version", row.get("appVersion"));
                item.put("osVersion", row.get("osVersion"));
                item.put("userCount", eventCount);
                item.put("activeUsers", eventCount);            // 兼容前端"活跃用户"列
                item.put("crashCount", crashCount);
                item.put("anrCount", safeInt(row, "anrCount"));
                item.put("lagCount", safeInt(row, "lagCount"));
                // 崩溃率口径：崩溃次数 / 启动次数（与看板 crash_count/session_count 一致）
                item.put("crashRate", startCount > 0 ? crashCount * 100.0 / startCount : 0.0);
                item.put("avgStartupTime", safeDouble(row, "avgStartupTime", 0));
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取设备维度数据失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Object getNetworkDistribution(DashboardQueryDTO queryDTO) {
        try {
            List<Map<String, Object>> rows = performanceDataMapperExt.selectNetworkDistribution(
                queryDTO.getAppKey(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
            );
            if (rows == null || rows.isEmpty()) {
                log.debug("网络维度数据为空，appKey={}", queryDTO.getAppKey());
                return new ArrayList<>();
            }
            return rows.stream().map(row -> {
                Map<String, Object> item = new HashMap<>();
                int eventCount = safeInt(row, "eventCount");
                int startCount = safeInt(row, "startCount");
                int networkTotal = safeInt(row, "networkTotal");
                int failureCount = safeInt(row, "networkFailureCount");
                item.put("networkType", row.get("networkType") != null ? row.get("networkType").toString() : "未知");
                item.put("version", row.get("networkType"));   // 前端表格通用列
                item.put("userCount", safeInt(row, "userCount"));
                item.put("activeUsers", safeInt(row, "userCount"));
                item.put("eventCount", eventCount);
                item.put("crashCount", safeInt(row, "crashCount"));
                item.put("anrCount", safeInt(row, "anrCount"));
                item.put("networkFailureCount", failureCount);
                item.put("crashRate", startCount > 0 ? safeInt(row, "crashCount") * 100.0 / startCount : 0.0);
                // 成功率分母必须是"网络事件总数"，不能用全部事件数，否则会系统性高估
                item.put("networkSuccessRate", networkTotal > 0 ? 100.0 - failureCount * 100.0 / networkTotal : 100.0);
                item.put("avgStartupTime", safeDouble(row, "avgStartupTime", 0));
                return item;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取网络维度数据失败", e);
            return new ArrayList<>();
        }
    }

    // ==================== 辅助方法 ====================

    private static int safeInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static double safeDouble(Map<String, Object> map, String key, int scale) {
        Object val = map.get(key);
        if (val == null) return 0.0;
        try {
            return new BigDecimal(val.toString()).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 将DTO转换为实体列表
     */
    private List<PerformanceDataNew> convertToEntityList(PerformanceDataReportDTO reportDTO) {
        List<PerformanceDataNew> dataList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (PerformanceDataReportDTO.PerformanceDataItem item : reportDTO.getData()) {
            PerformanceDataNew entity = new PerformanceDataNew();
            entity.setAppKey(reportDTO.getAppKey());
            entity.setDeviceId(item.getDeviceId());
            entity.setSessionId(item.getSessionId());
            entity.setEventType(item.getEventType());
            entity.setEventData(item.getData());
            entity.setEventTime(item.getEventTime());
            entity.setCreateTime(now);

            // 从附加信息中提取版本、系统版本和网络类型
            if (item.getExtras() != null) {
                JsonNode versionNode = item.getExtras().get("version");
                if (versionNode != null) {
                    entity.setVersionCode(versionNode.asText());
                }

                JsonNode osNode = item.getExtras().get("os");
                if (osNode != null) {
                    entity.setOsVersion(osNode.asText());
                }

                JsonNode networkNode = item.getExtras().get("network");
                if (networkNode != null) {
                    entity.setNetworkType(networkNode.asText());
                }

                // 省份：用于地域维度聚合（前端模拟器在 extras.province 中上报）
                JsonNode provinceNode = item.getExtras().get("province");
                if (provinceNode != null && !provinceNode.isNull()) {
                    entity.setProvince(provinceNode.asText());
                }
            }

            dataList.add(entity);
        }

        return dataList;
    }

    /**
     * 批量写入原始明细
     *
     * <p>这里不使用 MyBatis-Flex 的 insertBatch：其生成的 INSERT 列列表未包含后加的
     * province 列，会导致地域信息丢失、定时重算无法按省份聚合。
     * 改用 JdbcTemplate 显式指定列，保证 province 一并落库。
     */
    private void batchInsertRawData(List<PerformanceDataNew> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO performance_data "
                + "(app_key, device_id, session_id, event_type, event_time, event_data, "
                + "version_code, os_version, network_type, province, create_time) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                PerformanceDataNew d = dataList.get(i);
                LocalDateTime now = LocalDateTime.now();
                ps.setString(1, d.getAppKey());
                ps.setString(2, d.getDeviceId());
                ps.setString(3, d.getSessionId());
                ps.setString(4, d.getEventType());
                ps.setTimestamp(5, Timestamp.valueOf(d.getEventTime() != null ? d.getEventTime() : now));
                ps.setString(6, (d.getEventData() == null || d.getEventData().isNull())
                        ? null : d.getEventData().toString());
                ps.setString(7, d.getVersionCode());
                ps.setString(8, d.getOsVersion());
                ps.setString(9, d.getNetworkType());
                ps.setString(10, d.getProvince());
                ps.setTimestamp(11, Timestamp.valueOf(d.getCreateTime() != null ? d.getCreateTime() : now));
            }

            @Override
            public int getBatchSize() {
                return dataList.size();
            }
        });
    }

    /**
     * 处理数据聚合（上报时增量累加）
     *
     * <p>聚合维度：(日期, 省份, 应用版本)，写入 hour 为 NULL 的日汇总行。
     * 之所以采用日粒度而非小时粒度：performance_summary 的唯一键为 (app_key, date, hour)，
     * 写入具体 hour 时同一小时仅允许一行，无法再按省份/版本拆分；而 hour 为 NULL 时
     * MySQL 唯一索引不参与去重（NULL != NULL），可正常存放多省份、多版本行，
     * 与 DemoDataInitializer 生成的数据形态一致，仪表盘与地图查询均可直接命中。
     *
     * <p>注意：本方法由 batchSavePerformanceData 内部直接调用（this.xxx），
     * 标注 @Async 也不会经过 Spring 代理；外层方法已标注 @Async，整体对用户请求仍是异步的。
     */
    private void processDataAggregation(String appKey, List<PerformanceDataNew> dataList) {
        if (appKey == null || appKey.trim().isEmpty() || dataList == null || dataList.isEmpty()) {
            return;
        }
        try {
            Map<AggregateKey, AggregateBucket> buckets = new LinkedHashMap<>();
            // 同时维护「全国总览」维度（province / app_version 均为 NULL）：
            // 仪表盘 KPI 与告警指标快照读取的正是这一行，
            // 若只写带维度的行，上报数据既不会反映到仪表盘、也不会触发告警。
            Map<LocalDate, AggregateBucket> overviewBuckets = new LinkedHashMap<>();

            for (PerformanceDataNew d : dataList) {
                LocalDateTime eventTime = d.getEventTime() != null ? d.getEventTime() : LocalDateTime.now();
                LocalDate date = eventTime.toLocalDate();
                AggregateKey key = new AggregateKey(date,
                        blankToNull(d.getProvince()), blankToNull(d.getVersionCode()));
                buckets.computeIfAbsent(key, k -> new AggregateBucket()).collect(d);
                overviewBuckets.computeIfAbsent(date, k -> new AggregateBucket()).collect(d);
            }
            for (Map.Entry<AggregateKey, AggregateBucket> entry : buckets.entrySet()) {
                upsertDailySummary(appKey, entry.getKey(), entry.getValue());
            }
            for (Map.Entry<LocalDate, AggregateBucket> entry : overviewBuckets.entrySet()) {
                upsertDailySummary(appKey, new AggregateKey(entry.getKey(), null, null), entry.getValue());
            }
            log.info("数据聚合完成，应用：{}，分组数：{}", appKey, buckets.size());
        } catch (Exception e) {
            // 聚合失败不得影响已入库的原始数据
            log.error("数据聚合失败（原始数据已入库），应用：{}", appKey, e);
        }
    }

    /**
     * 重算指定应用某天的汇总数据（幂等：先删后插）
     *
     * 供定时任务调用。上报时是增量累加，可能存在统计偏差；
     * 定时任务基于原始数据重算，保证最终数据准确。
     */
    public void recomputeDailySummary(String appKey, LocalDate date) {
        if (appKey == null || appKey.trim().isEmpty() || date == null) {
            return;
        }
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT IFNULL(province,'') AS province, IFNULL(version_code,'') AS version, "
                            + "COUNT(DISTINCT device_id) AS active_users, COUNT(DISTINCT session_id) AS session_count, "
                            + "SUM(CASE WHEN UPPER(event_type) IN ('APP_CRASH','CRASH') THEN 1 ELSE 0 END) AS crash_count, "
                            + "SUM(CASE WHEN UPPER(event_type) = 'ANR' THEN 1 ELSE 0 END) AS anr_count, "
                            + "SUM(CASE WHEN UPPER(event_type) IN ('LOW_FPS','LAG') THEN 1 ELSE 0 END) AS lag_count, "
                            + "SUM(CASE WHEN UPPER(event_type) = 'NETWORK' THEN 1 ELSE 0 END) AS network_request_count, "
                            + "SUM(CASE WHEN UPPER(event_type) = 'NETWORK' "
                            + "         AND IFNULL(JSON_UNQUOTE(JSON_EXTRACT(event_data,'$.success')),'true') = 'false' "
                            + "         THEN 1 ELSE 0 END) AS network_failure_count, "
                            + "AVG(CASE WHEN UPPER(event_type) IN ('APP_START','STARTUP') "
                            + "         THEN JSON_UNQUOTE(JSON_EXTRACT(event_data,'$.startupTime')) END) AS avg_startup_time, "
                            + "AVG(CASE WHEN UPPER(event_type) = 'CPU' "
                            + "         THEN JSON_UNQUOTE(JSON_EXTRACT(event_data,'$.cpuUsage')) END) AS avg_cpu_usage, "
                            + "AVG(CASE WHEN UPPER(event_type) = 'MEMORY' "
                            + "         THEN JSON_UNQUOTE(JSON_EXTRACT(event_data,'$.usedMemoryMB')) END) AS avg_memory_usage "
                            + "FROM performance_data WHERE app_key = ? AND DATE(event_time) = ? "
                            + "GROUP BY IFNULL(province,''), IFNULL(version_code,'')",
                    appKey, date);

            // 幂等：先删除该应用当天的日汇总行，再按重算结果重新插入
            jdbcTemplate.update("DELETE FROM performance_summary WHERE app_key = ? AND date = ? AND hour IS NULL",
                    appKey, date);

            for (Map<String, Object> r : rows) {
                jdbcTemplate.update(
                        "INSERT INTO performance_summary (app_key, date, hour, province, app_version, active_users, "
                                + "new_users, session_count, crash_count, anr_count, lag_count, network_request_count, "
                                + "network_failure_count, avg_startup_time, avg_cpu_usage, avg_memory_usage, "
                                + "create_time, update_time) VALUES (?,?,NULL,NULLIF(?,''),NULLIF(?,''),?,0,?,?,?,?,?,?,?,?,?,NOW(),NOW())",
                        appKey, date, str(r.get("province")), str(r.get("version")),
                        intVal(r.get("active_users")), intVal(r.get("session_count")),
                        intVal(r.get("crash_count")), intVal(r.get("anr_count")), intVal(r.get("lag_count")),
                        intVal(r.get("network_request_count")), intVal(r.get("network_failure_count")),
                        r.get("avg_startup_time"), r.get("avg_cpu_usage"), r.get("avg_memory_usage"));
            }
            log.info("重算日汇总完成，应用：{}，日期：{}，分组数：{}", appKey, date, rows.size());
        } catch (Exception e) {
            log.error("重算日汇总失败，应用：{}，日期：{}", appKey, date, e);
        }
    }

    /**
     * 写入/累加某天的汇总行
     */
    private void upsertDailySummary(String appKey, AggregateKey key, AggregateBucket bucket) {
        String provinceKey = key.province == null ? "" : key.province;
        String versionKey = key.version == null ? "" : key.version;
        Integer id = findSummaryId(appKey, key.date, provinceKey, versionKey);

        BigDecimal avgStartup = bucket.avgOf(bucket.startupSum, bucket.startupCnt);
        BigDecimal avgCpu = bucket.avgOf(bucket.cpuSum, bucket.cpuCnt);
        BigDecimal avgMem = bucket.avgOf(bucket.memSum, bucket.memCnt);

        if (id != null) {
            StringBuilder sql = new StringBuilder("UPDATE performance_summary SET ")
                    .append("active_users = active_users + ?, session_count = session_count + ?, ")
                    .append("crash_count = crash_count + ?, anr_count = anr_count + ?, lag_count = lag_count + ?, ")
                    .append("network_request_count = network_request_count + ?, ")
                    .append("network_failure_count = network_failure_count + ?, update_time = NOW()");
            List<Object> args = new ArrayList<>();
            args.add(bucket.activeUsers());
            args.add(bucket.sessionCount());
            args.add(bucket.crashCount);
            args.add(bucket.anrCount);
            args.add(bucket.lagCount);
            args.add(bucket.networkCount);
            args.add(bucket.networkFailCount);
            // 平均值类指标：增量场景无历史样本数，本次有样本时以本次均值覆盖（近似）
            if (avgStartup != null) { sql.append(", avg_startup_time = ?"); args.add(avgStartup); }
            if (avgCpu != null) { sql.append(", avg_cpu_usage = ?"); args.add(avgCpu); }
            if (avgMem != null) { sql.append(", avg_memory_usage = ?"); args.add(avgMem); }
            sql.append(" WHERE id = ?");
            args.add(id);
            jdbcTemplate.update(sql.toString(), args.toArray());
        } else {
            jdbcTemplate.update(
                    "INSERT INTO performance_summary (app_key, date, hour, province, app_version, active_users, "
                            + "new_users, session_count, crash_count, anr_count, lag_count, network_request_count, "
                            + "network_failure_count, avg_startup_time, avg_cpu_usage, avg_memory_usage, "
                            + "create_time, update_time) VALUES (?,?,NULL,?,?,?,0,?,?,?,?,?,?,?,?,?,NOW(),NOW())",
                    appKey, key.date, key.province, key.version,
                    bucket.activeUsers(), bucket.sessionCount(),
                    bucket.crashCount, bucket.anrCount, bucket.lagCount,
                    bucket.networkCount, bucket.networkFailCount,
                    avgStartup, avgCpu, avgMem);
        }
    }

    private Integer findSummaryId(String appKey, LocalDate date, String provinceKey, String versionKey) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id FROM performance_summary WHERE app_key = ? AND date = ? AND hour IS NULL "
                            + "AND IFNULL(province,'') = ? AND IFNULL(app_version,'') = ? LIMIT 1",
                    Integer.class, appKey, date, provinceKey, versionKey);
        } catch (Exception e) {
            return null; // 未命中表示该维度尚无汇总行
        }
    }

    /**
     * 检查告警：聚合完成后基于当天汇总指标匹配已启用的告警规则
     *
     * 触发后的实例创建、通知发送与 WebSocket 推送由 AlertRuleEngine#evaluateRules 内部完成。
     */
    private void checkAlerts(String appKey, List<PerformanceDataNew> dataList) {
        if (appKey == null || appKey.trim().isEmpty() || dataList == null || dataList.isEmpty()) {
            return;
        }
        try {
            List<AlertRule> rules = alertRuleMapper.selectEnabledByAppKey(appKey);
            if (rules == null || rules.isEmpty()) {
                return;
            }
            Map<String, Number> metrics = buildMetricSnapshot(appKey);
            if (metrics.isEmpty()) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            for (AlertRule rule : rules) {
                Number value = metrics.get(normalizeMetric(rule.getMetric()));
                if (value == null) {
                    continue;
                }
                // 同一规则已存在未结束告警时跳过，避免重复触发造成告警风暴
                if (hasActiveAlert(rule.getId())) {
                    continue;
                }
                MetricDataDTO dto = new MetricDataDTO();
                dto.setAppKey(appKey);
                dto.setMetric(rule.getMetric());
                dto.setValue(value);
                dto.setTimestamp(now);
                // 只把该规则对应的指标交给引擎，避免规则与指标交叉匹配导致误报
                alertRuleEngine.evaluateRules(Collections.singletonList(rule), Collections.singletonList(dto));
            }
        } catch (Exception e) {
            log.error("告警检测失败（不影响数据入库），应用：{}", appKey, e);
        }
    }

    /**
     * 基于当天全国汇总行构造指标快照
     */
    private Map<String, Number> buildMetricSnapshot(String appKey) {
        Map<String, Number> metrics = new HashMap<>();
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap(
                    "SELECT IFNULL(SUM(session_count),0) AS sessions, IFNULL(SUM(crash_count),0) AS crashes, "
                            + "IFNULL(SUM(lag_count),0) AS lags, IFNULL(SUM(anr_count),0) AS anrs, "
                            + "IFNULL(SUM(network_request_count),0) AS reqs, IFNULL(SUM(network_failure_count),0) AS fails, "
                            + "IFNULL(AVG(avg_startup_time),0) AS startup, IFNULL(AVG(avg_memory_usage),0) AS mem, "
                            + "IFNULL(AVG(avg_cpu_usage),0) AS cpu "
                            + "FROM performance_summary WHERE app_key = ? AND date = ? AND hour IS NULL "
                            + "AND province IS NULL AND (app_version IS NULL OR app_version = '')",
                    appKey, LocalDate.now());

            double sessions = toDouble(row.get("sessions"));
            double crashes = toDouble(row.get("crashes"));
            double lags = toDouble(row.get("lags"));
            double anrs = toDouble(row.get("anrs"));
            double reqs = toDouble(row.get("reqs"));
            double fails = toDouble(row.get("fails"));

            if (sessions > 0) {
                metrics.put("crashrate", round2(crashes * 100.0 / sessions));
                metrics.put("lagrate", round2(lags * 100.0 / sessions));
                metrics.put("anrrate", round2(anrs * 100.0 / sessions));
            }
            if (reqs > 0) {
                metrics.put("networksuccessrate", round2((reqs - fails) * 100.0 / reqs));
                metrics.put("networkfailurerate", round2(fails * 100.0 / reqs));
            }
            metrics.put("startuptime", round2(toDouble(row.get("startup"))));
            metrics.put("memoryusage", round2(toDouble(row.get("mem"))));
            metrics.put("cpuusage", round2(toDouble(row.get("cpu"))));
        } catch (Exception e) {
            log.debug("构建指标快照失败（当天可能暂无汇总数据）：{}", e.getMessage());
        }
        return metrics;
    }

    /** 同一规则是否已有未结束的告警 */
    private boolean hasActiveAlert(Long ruleId) {
        if (ruleId == null) {
            return false;
        }
        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(1) FROM alert_instance WHERE rule_id = ? AND status IN ('FIRING','ACKNOWLEDGED')",
                    Integer.class, ruleId);
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** 指标名归一：兼容 crashRate / crash_rate / CRASH-RATE 等写法 */
    private static String normalizeMetric(String metric) {
        return metric == null ? "" : metric.toLowerCase().replace("_", "").replace("-", "");
    }

    private static String blankToNull(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    private static boolean isNetworkFailed(JsonNode data) {
        if (data == null) {
            return false;
        }
        JsonNode node = data.get("success");
        if (node == null) {
            return false;
        }
        if (node.isBoolean()) {
            return !node.asBoolean();
        }
        return "false".equalsIgnoreCase(node.asText());
    }

    /** 依次尝试多个字段名读取数值，兼容不同上报端的字段命名 */
    private static Double readNumber(JsonNode data, String... fields) {
        if (data == null || fields == null) {
            return null;
        }
        for (String f : fields) {
            JsonNode node = data.get(f);
            if (node != null && (node.isNumber() || node.isTextual())) {
                try {
                    return Double.valueOf(node.asText());
                } catch (NumberFormatException ignored) {
                    // 尝试下一个字段名
                }
            }
        }
        return null;
    }

    private static double toDouble(Object v) {
        if (v == null) {
            return 0.0;
        }
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        try {
            return Double.parseDouble(v.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static double round2(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private static int intVal(Object v) {
        return v == null ? 0 : ((Number) v).intValue();
    }

    private static String str(Object v) {
        return v == null ? null : v.toString();
    }

    /** 聚合维度键：日期 + 省份 + 版本 */
    private static class AggregateKey {
        final LocalDate date;
        final String province;
        final String version;

        AggregateKey(LocalDate date, String province, String version) {
            this.date = date;
            this.province = province;
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AggregateKey)) return false;
            AggregateKey other = (AggregateKey) o;
            return java.util.Objects.equals(date, other.date)
                    && java.util.Objects.equals(province, other.province)
                    && java.util.Objects.equals(version, other.version);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(date, province, version);
        }
    }

    /** 聚合桶：累计一个维度下的各项指标 */
    private static class AggregateBucket {
        final Set<String> devices = new HashSet<>();
        final Set<String> sessions = new HashSet<>();
        int crashCount;
        int anrCount;
        int lagCount;
        int networkCount;
        int networkFailCount;
        double startupSum;
        double cpuSum;
        double memSum;
        int startupCnt;
        int cpuCnt;
        int memCnt;

        void collect(PerformanceDataNew d) {
            if (d.getDeviceId() != null) devices.add(d.getDeviceId());
            if (d.getSessionId() != null) sessions.add(d.getSessionId());

            String type = d.getEventType() == null ? "" : d.getEventType().trim().toUpperCase();
            JsonNode data = d.getEventData();
            switch (type) {
                case "APP_CRASH":
                case "CRASH":
                    crashCount++;
                    break;
                case "ANR":
                    anrCount++;
                    break;
                case "LOW_FPS":
                case "LAG":
                    lagCount++;
                    break;
                case "NETWORK":
                    networkCount++;
                    if (isNetworkFailed(data)) {
                        networkFailCount++;
                    }
                    break;
                case "APP_START":
                case "STARTUP": {
                    Double v = readNumber(data, "startupTime", "duration");
                    if (v != null) { startupSum += v; startupCnt++; }
                    break;
                }
                case "CPU": {
                    Double v = readNumber(data, "cpuUsage", "cpu");
                    if (v != null) { cpuSum += v; cpuCnt++; }
                    break;
                }
                case "MEMORY": {
                    Double v = readNumber(data, "usedMemoryMB", "memoryUsage", "memory");
                    if (v != null) { memSum += v; memCnt++; }
                    break;
                }
                default:
                    break;
            }
        }

        int activeUsers() {
            return devices.size();
        }

        int sessionCount() {
            return sessions.size();
        }

        BigDecimal avgOf(double sum, int cnt) {
            return cnt > 0 ? BigDecimal.valueOf(sum / cnt).setScale(1, RoundingMode.HALF_UP) : null;
        }
    }
}
