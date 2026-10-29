package com.monitor.config;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.monitor.service.PerformanceDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 性能数据定时聚合任务（任务指导书功能点 20）
 *
 * <p>策略：
 * <ul>
 *   <li><b>每小时增量</b>：重算当天汇总，把最近一小时新增的原始明细合并进来，
 *       同时修正上报时增量累加可能产生的偏差。</li>
 *   <li><b>每日全量</b>：凌晨重算昨天全天汇总，保证最终数据准确。</li>
 * </ul>
 *
 * <p>重算采用「先删后插」的幂等方式，重复执行不会产生重复数据。
 *
 * <p><b>注意</b>：演示应用 {@code test-app} 的汇总数据由 {@link DemoDataInitializer} 直接生成
 * （并非由原始明细推导），若对其执行重算会把演示数据覆盖成明细量的真实值，
 * 导致仪表盘数字骤降。因此定时任务跳过该应用；
 * 该应用在上报时的增量聚合仍然生效，可正常验证「上报 → 聚合 → 告警」链路。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataAggregationScheduler {

    /** 演示应用标识：其汇总数据由 DemoDataInitializer 维护，跳过定时重算 */
    private static final String DEMO_APP_KEY = "test-app";

    private final JdbcTemplate jdbcTemplate;
    private final PerformanceDataService performanceDataService;

    /**
     * 每小时第 5 分钟执行：增量聚合（重算当天）
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void aggregateTodayHourly() {
        LocalDate date = LocalDate.now();
        log.info("[定时聚合] 增量聚合完成（当天 {}），处理应用数：{}", date, aggregate(date));
    }

    /**
     * 每日凌晨 1:30 执行：全量聚合（重算昨天）
     */
    @Scheduled(cron = "0 30 1 * * ?")
    public void aggregateYesterdayDaily() {
        LocalDate date = LocalDate.now().minusDays(1);
        log.info("[定时聚合] 全量聚合完成（昨天 {}），处理应用数：{}", date, aggregate(date));
    }

    /**
     * 对指定日期执行聚合，返回实际处理的应用数
     */
    private int aggregate(LocalDate date) {
        int count = 0;
        for (String appKey : listAppKeys()) {
            if (DEMO_APP_KEY.equals(appKey)) {
                continue;
            }
            performanceDataService.recomputeDailySummary(appKey, date);
            count++;
        }
        return count;
    }

    /**
     * 取「已注册应用」与「近期有数据上报的应用」的并集，避免遗漏未注册却有上报的应用
     */
    private List<String> listAppKeys() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT DISTINCT app_key FROM ("
                            + "  SELECT app_key FROM application WHERE IFNULL(deleted, 0) = 0"
                            + "  UNION"
                            + "  SELECT DISTINCT app_key FROM performance_data WHERE event_time >= ?"
                            + ") t",
                    String.class,
                    LocalDate.now().minusDays(2).atStartOfDay());
        } catch (Exception e) {
            log.warn("[定时聚合] 获取应用列表失败：{}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
