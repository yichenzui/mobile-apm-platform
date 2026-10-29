package com.monitor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 演示数据初始化器：每次启动时自动基于当前日期生成完整的模拟数据
 * 覆盖 performance_summary、performance_data、alert_instance、issue 和 Redis 在线用户
 *
 * <p><b>安全说明</b>：该初始化器会先删除 app_key='test-app' 的旧数据再重建，
 * 仅适用于开发 / 演示环境。由 {@code demo.data.enabled} 开关控制：开发环境默认开启，
 * 部署到生产时请在配置中设置 {@code demo.data.enabled=false} 关闭，
 * 否则每次重启都会清空并重建演示数据。
 *
 * <p>此处使用 {@code @ConditionalOnProperty} 而非 {@code @Profile("dev")}：
 * 本项目 dev profile 并非在所有启动方式下都被激活（IDEA 直跑 / mvn spring-boot:run 存在差异），
 * 若直接限定 {@code @Profile("dev")}，在 profile 未激活时演示数据会静默消失、仪表盘变空。
 */
@Component
@ConditionalOnProperty(prefix = "demo.data", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private final Random random = new Random();
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 省份数据 [省份名称, 活跃用户, 新增用户, 会话数, 崩溃次数, 卡顿数, 网络请求, 网络失败]
    // 名称必须与 china-simple.json 中的地图区域名完全一致
    private static final Object[][] PROVINCE_DATA = {
            {"广东省", 280, 20, 720, 2, 8, 10800, 210},
            {"北京", 220, 15, 560, 3, 7, 8400, 180},
            {"上海", 180, 12, 450, 1, 5, 6750, 130},
            {"浙江省", 150, 10, 380, 2, 6, 5700, 120},
            {"江苏省", 120, 8, 310, 1, 4, 4650, 90},
            {"四川省", 100, 7, 260, 1, 4, 3900, 75},
            {"湖北省", 90, 6, 230, 1, 5, 3450, 70},
            {"山东省", 85, 5, 210, 1, 3, 3150, 60},
            {"河南省", 80, 5, 200, 1, 3, 3000, 55},
            {"湖南省", 65, 4, 170, 1, 3, 2550, 50},
            {"福建省", 55, 3, 140, 0, 2, 2100, 40},
            {"安徽省", 50, 3, 130, 0, 2, 1950, 35},
            {"辽宁省", 45, 2, 120, 0, 2, 1800, 30},
            {"陕西省", 40, 2, 105, 0, 1, 1575, 25},
            {"重庆", 35, 2, 95, 0, 1, 1425, 20},
            {"天津", 48, 3, 123, 0, 2, 1840, 35},
            {"河北省", 60, 4, 155, 1, 3, 2300, 45},
            {"山西省", 38, 2, 98, 0, 2, 1450, 28},
            {"内蒙古自治区", 30, 2, 78, 0, 1, 1150, 22},
            {"吉林省", 32, 2, 82, 0, 1, 1220, 24},
            {"黑龙江省", 30, 2, 78, 0, 1, 1150, 22},
            {"江西省", 42, 3, 108, 0, 2, 1600, 30},
            {"广西壮族自治区", 33, 2, 85, 0, 1, 1260, 25},
            {"海南省", 25, 2, 65, 0, 1, 950, 18},
            {"贵州省", 28, 2, 72, 0, 1, 1070, 20},
            {"云南省", 30, 2, 78, 0, 1, 1150, 22},
            {"甘肃省", 20, 1, 52, 0, 1, 760, 15},
            {"青海省", 12, 1, 31, 0, 0, 460, 9},
            {"宁夏回族自治区", 10, 1, 26, 0, 0, 380, 7},
            {"新疆维吾尔自治区", 18, 1, 46, 0, 1, 690, 13},
            {"西藏自治区", 6, 1, 15, 0, 0, 230, 4},
            {"台湾", 15, 1, 38, 0, 1, 580, 11},
            {"香港", 20, 1, 51, 0, 1, 770, 15},
            {"澳门", 5, 1, 13, 0, 0, 190, 3}
    };

    // 设备型号池
    private static final String[] DEVICE_MODELS = {
            "iPhone 15 Pro Max", "iPhone 15 Pro", "iPhone 14 Pro", "iPhone 14",
            "iPhone 13", "Samsung Galaxy S24", "Xiaomi 14 Pro", "Huawei Mate 60 Pro",
            "OPPO Find X7", "vivo X100 Pro"
    };

    // 系统版本池
    private static final String[] OS_VERSIONS = {
            "iOS 17.4", "iOS 17.0", "iOS 16.6", "Android 14", "Android 13",
            "Android 12", "HarmonyOS 4.0", "HarmonyOS 3.0"
    };

    // 网络类型
    private static final String[] NETWORK_TYPES = {"WIFI", "4G", "5G", "WIFI", "WIFI", "5G", "4G"};

    // 应用版本
    private static final String[] APP_VERSIONS = {"2.3.0", "2.3.1", "2.4.0"};

    // 省份列表（用于随机选择设备归属地）
    private static final String[] PROVINCES = {
            "广东省", "北京", "上海", "浙江省", "江苏省", "四川省", "湖北省",
            "山东省", "河南省", "湖南省", "福建省", "安徽省", "辽宁省", "陕西省", "重庆市"
    };

    // 城市列表（对应省份）
    private static final String[][] CITIES = {
            {"深圳", "广州", "东莞"}, {"北京"}, {"上海"},
            {"杭州", "宁波", "温州"}, {"南京", "苏州", "无锡"},
            {"成都", "绵阳"}, {"武汉", "宜昌", "襄阳"},
            {"济南", "青岛", "烟台"}, {"郑州", "洛阳"},
            {"长沙", "株洲"}, {"福州", "厦门"}, {"合肥"},
            {"沈阳", "大连"}, {"西安"}, {"重庆"}
    };

    public DemoDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            ensureColumnsExist();
            generateAllDemoData();
        } catch (Exception e) {
            log.error("演示数据初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 确保 performance_summary 表包含所需的所有列，缺少则 ALTER TABLE 添加
     */
    private void ensureColumnsExist() {
        try {
            // 尝试查询列是否存在，若不存在则添加（忽略已存在的错误）
            try { jdbcTemplate.execute("ALTER TABLE performance_summary ADD COLUMN anr_count INT DEFAULT 0 COMMENT 'ANR次数'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE performance_summary ADD COLUMN app_version VARCHAR(20) DEFAULT NULL COMMENT '应用版本'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE performance_summary ADD COLUMN avg_startup_time DECIMAL(10,1) DEFAULT 0 COMMENT '平均启动时间(ms)'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE performance_summary ADD COLUMN avg_cpu_usage DECIMAL(5,1) DEFAULT 0 COMMENT '平均CPU使用率(%)'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE performance_summary ADD COLUMN avg_memory_usage DECIMAL(10,1) DEFAULT 0 COMMENT '平均内存使用(MB)'"); } catch (Exception ignored) {}
            try { jdbcTemplate.execute("ALTER TABLE performance_summary ADD COLUMN lag_count INT DEFAULT 0 COMMENT '卡顿次数'"); } catch (Exception ignored) {}
            // performance_data 的省份列：供上报数据按地域聚合使用（老库升级时需要）
            try { jdbcTemplate.execute("ALTER TABLE performance_data ADD COLUMN province VARCHAR(50) DEFAULT NULL COMMENT '省份'"); } catch (Exception ignored) {}
            log.info("✓ 数据库列已就绪");
        } catch (Exception e) {
            log.warn("数据库列检查异常（可能已存在）: {}", e.getMessage());
        }
    }

    private void generateAllDemoData() {
        LocalDate today = LocalDate.now();
        int totalRows = 0;

        // ========== 1. 清理旧演示数据 ==========
        jdbcTemplate.update("DELETE FROM performance_summary WHERE app_key = 'test-app'");
        jdbcTemplate.update("DELETE FROM performance_data WHERE app_key = 'test-app'");
        jdbcTemplate.update("DELETE FROM alert_instance WHERE app_key = 'test-app'");
        jdbcTemplate.update("DELETE FROM issue WHERE app_key = 'test-app'");
        log.info("已清理 test-app 旧演示数据");

        // ========== 2. 生成 performance_summary 汇总数据 ==========
        totalRows += generateSummaryData(today);

        // ========== 3. 生成 performance_data 明细数据 ==========
        totalRows += generatePerformanceData(today);

        // ========== 4. 生成告警实例数据 ==========
        totalRows += generateAlertData(today);

        // ========== 5. 生成问题追踪数据 ==========
        totalRows += generateIssueData(today);

        // ========== 6. 补充演示用告警规则（已存在则不覆盖，避免冲掉用户自定义规则） ==========
        totalRows += generateAlertRules();

        // ========== 7. 写入 Redis 在线用户数 ==========
        writeRedisOnlineUsers();

        log.info("================================================");
        log.info(" 演示数据全部生成完毕，共 {} 条记录", totalRows);
        log.info(" 涵盖: 汇总表 + 明细表 + 告警 + 问题 + 告警规则 + Redis在线用户");
        log.info("================================================");
    }

    // ==================== 告警规则 ====================

    /**
     * 补充演示应用 test-app 的告警规则，打通「上报 → 聚合 → 告警检测」链路。
     *
     * <p>告警规则表默认没有任何数据，若缺少规则，数据上报后的检测环节会直接返回，
     * 整条链路无法验证。此处仅在 test-app 尚无任何规则时写入，用户后续自定义或
     * 调整过的规则不会被覆盖。
     *
     * <p>阈值说明：演示数据当天的崩溃率约为 0.37%，因此崩溃率规则（&gt; 0.3%）会在
     * 上报后触发，便于直观看到告警产生；其余规则的阈值均高于当前演示值，保持
     * 「正常不告警、异常才告警」的效果。
     */
    private int generateAlertRules() {
        try {
            Integer exists = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM alert_rule WHERE app_key = 'test-app'", Integer.class);
            if (exists != null && exists > 0) {
                log.info("test-app 已存在告警规则 {} 条，跳过初始化", exists);
                return 0;
            }

            // {规则名, 指标, 操作符, 阈值, 严重程度}
            Object[][] rules = {
                    {"崩溃率异常告警", "CRASH_RATE", "gt", 0.3, "HIGH"},
                    {"ANR率异常告警", "ANR_RATE", "gt", 0.2, "MEDIUM"},
                    {"启动耗时过慢告警", "STARTUP_TIME", "gt", 2500, "MEDIUM"},
                    {"CPU使用率过高告警", "CPU_USAGE", "gt", 80, "MEDIUM"},
                    {"内存占用过高告警", "MEMORY_USAGE", "gt", 350, "MEDIUM"},
                    {"网络成功率下降告警", "NETWORK_SUCCESS_RATE", "lt", 97, "HIGH"}
            };

            String sql = "INSERT INTO alert_rule (app_key, name, rule_name, rule_type, metric, `condition`, "
                    + "condition_config, threshold, duration, severity, status, enabled, "
                    + "notification_channels, notification_config, deleted) "
                    + "VALUES ('test-app', ?, ?, 'THRESHOLD', ?, ?, ?, ?, 5, ?, 1, 1, "
                    + "'[\"email\"]', '{}', 0)";

            for (Object[] r : rules) {
                String operator = (String) r[2];
                jdbcTemplate.update(sql,
                        r[0],                                                   // name
                        r[0],                                                   // rule_name
                        r[1],                                                   // metric
                        operator.toUpperCase(),                                 // condition
                        String.format("{\"operator\":\"%s\",\"threshold\":%s}",  // condition_config
                                operator, r[3]),
                        r[3],                                                   // threshold
                        r[4]);                                                  // severity
            }
            log.info("✓ 已初始化 test-app 告警规则 {} 条", rules.length);
            return rules.length;
        } catch (Exception e) {
            log.warn("初始化告警规则失败（不影响其余演示数据）：{}", e.getMessage());
            return 0;
        }
    }

    // ==================== 汇总数据 ====================

    private int generateSummaryData(LocalDate today) {
        // 每日全国汇总数据 [活跃用户, 新增用户, 会话数, 崩溃次数, ANR次数, 卡顿数,
        //                      网络请求, 网络失败, 平均启动ms, 平均CPU%, 平均内存MB]
        int[][] dailyData = {
                {1250, 85, 3200, 12, 3, 45, 48000, 960, 1850, 35, 210},
                {1180, 72, 3050, 10, 2, 38, 45200, 880, 1920, 32, 195},
                {1320, 95, 3400, 15, 4, 52, 51000, 1050, 1780, 38, 225},
                {1100, 68, 2900, 8, 2, 32, 42000, 750, 2010, 30, 188},
                {1400, 102, 3650, 18, 5, 58, 55000, 1200, 1700, 40, 240},
                {1280, 88, 3300, 14, 3, 48, 49500, 1000, 1880, 36, 215},
                {1150, 78, 3000, 11, 3, 40, 45000, 900, 1950, 33, 200}
        };

        String sql = "INSERT INTO performance_summary " +
                "(app_key, date, app_version, province, active_users, new_users, session_count, " +
                "crash_count, anr_count, lag_count, " +
                "network_request_count, network_failure_count, " +
                "avg_startup_time, avg_cpu_usage, avg_memory_usage) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int count = 0;

        // ===== 7天全国汇总 (province=NULL, app_version=NULL) =====
        for (int i = 0; i < dailyData.length; i++) {
            LocalDate date = today.minusDays(i);
            int[] d = dailyData[i];
            jdbcTemplate.update(sql, "test-app", date, null, null,
                    d[0], d[1], d[2], d[3], d[4], d[5], d[6], d[7], d[8], d[9], d[10]);
            count++;
        }

        // ===== 7天 × 省份分布 (app_version=NULL) =====
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate date = today.minusDays(dayOffset);
            for (int pi = 0; pi < PROVINCE_DATA.length; pi++) {
                Object[] p = PROVINCE_DATA[pi];
                double ratio = 0.7 + random.nextDouble() * 0.6; // 0.7-1.3 波动
                int au = (int)(((Number) p[1]).intValue() * ratio);
                int nu = (int)(((Number) p[2]).intValue() * ratio);
                int sc = (int)(((Number) p[3]).intValue() * ratio);
                int cr = (int)(((Number) p[4]).intValue() * ratio);
                int an = Math.max(0, cr / 3 + random.nextInt(2));
                int lg = (int)(((Number) p[5]).intValue() * ratio);
                int nr = (int)(((Number) p[6]).intValue() * ratio);
                int nf = (int)(((Number) p[7]).intValue() * ratio);
                int st = 1500 + random.nextInt(2500);
                int cpu = 20 + random.nextInt(40);
                int mem = 120 + random.nextInt(250);
                jdbcTemplate.update(sql, "test-app", date, null, p[0],
                        au, nu, sc, cr, an, lg, nr, nf, st, cpu, mem);
                count++;
            }
        }

        // ===== 7天 × 3版本分布 (province=NULL, app_version=NOT NULL) =====
        double[][] versionRatios = {
                {0.45, 0.40, 0.38, 0.40, 0.42, 0.40, 0.38}, // 2.3.0 份额逐渐下降
                {0.25, 0.28, 0.30, 0.30, 0.28, 0.30, 0.28}, // 2.3.1 稳定
                {0.30, 0.32, 0.32, 0.30, 0.30, 0.30, 0.34}  // 2.4.0 新版本增长
        };
        // 每个版本的质量参数: [启动时间倍率, CPU倍率, 内存倍率, 崩溃次数倍率, ANR倍率]
        double[][] versionQuality = {
                {1.10, 1.05, 1.10, 0.7, 0.7},  // 2.3.0 较旧，启动稍慢
                {1.00, 1.00, 1.00, 1.0, 1.0},  // 2.3.1 基准
                {0.85, 0.90, 0.85, 1.3, 1.4}   // 2.4.0 新版本启动快但稳定性略差
        };

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate date = today.minusDays(dayOffset);
            int[] d = dailyData[dayOffset];
            for (int vi = 0; vi < APP_VERSIONS.length; vi++) {
                String ver = APP_VERSIONS[vi];
                double share = versionRatios[vi][dayOffset];
                int au = (int)(d[0] * share);
                int nu = (int)(d[1] * share);
                int sc = (int)(d[2] * share);
                int cr = Math.max(0, (int)(d[3] * share * versionQuality[vi][3]));
                int an = Math.max(0, (int)(d[4] * share * versionQuality[vi][4]));
                int lg = (int)(d[5] * share);
                int nr = (int)(d[6] * share);
                int nf = (int)(d[7] * share);
                int st = (int)(d[8] * versionQuality[vi][0]);
                int cpu = (int)(d[9] * versionQuality[vi][1]);
                int mem = (int)(d[10] * versionQuality[vi][2]);
                jdbcTemplate.update(sql, "test-app", date, ver, null,
                        au, nu, sc, cr, an, lg, nr, nf, st, cpu, mem);
                count++;
            }
        }

        log.info("✓ performance_summary: {} 条（7天全国 + {} 省份 × 7天 + {} 版本 × 7天）",
                count, PROVINCE_DATA.length, APP_VERSIONS.length);
        return count;
    }

    // ==================== 明细数据 (performance_data) ====================

    private int generatePerformanceData(LocalDate today) {
        String sql = "INSERT INTO performance_data " +
                "(app_key, device_id, user_id, session_id, event_type, event_time, event_data, " +
                "version_code, os_version, network_type, create_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int count = 0;

        // 生成10个设备，每个设备多天多事件
        for (int devIdx = 0; devIdx < 10; devIdx++) {
            String deviceId = "DEV-" + String.format("%04X", devIdx * 100 + random.nextInt(256));
            String userId = "USER-" + (1000 + devIdx);
            String osVersion = OS_VERSIONS[random.nextInt(OS_VERSIONS.length)];
            String versionCode = APP_VERSIONS[random.nextInt(APP_VERSIONS.length)];

            // 每个设备生成 7 天的事件
            for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
                LocalDate date = today.minusDays(dayOffset);
                String sessionId = "SESS-" + date + "-" + String.format("%04d", devIdx * 10 + dayOffset);

                // -- APP_START 事件（每天1-2个） --
                int startsToday = 1 + random.nextInt(2);
                for (int s = 0; s < startsToday; s++) {
                    int startupTime = 800 + random.nextInt(4200); // 800-5000ms
                    String eventData = "{\"startupTime\":" + startupTime
                            + ",\"startupType\":\"" + (random.nextDouble() > 0.3 ? "COLD" : "WARM") + "\"}";
                    LocalDateTime eventTime = date.atTime(8 + random.nextInt(14), random.nextInt(60), random.nextInt(60));
                    jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                            "APP_START", eventTime.format(DT_FORMAT), eventData,
                            versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                    count++;
                }

                // -- CPU 事件（每天1个，带FPS） --
                int fps = 25 + random.nextInt(36); // 25-60
                String cpuData = "{\"cpuUsage\":" + (10 + random.nextInt(60))
                        + ",\"fps\":" + fps + ",\"batteryTemp\":" + (25 + random.nextInt(20)) + "}";
                LocalDateTime cpuTime = date.atTime(10 + random.nextInt(10), random.nextInt(60), random.nextInt(60));
                jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                        "CPU", cpuTime.format(DT_FORMAT), cpuData,
                        versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                count++;

                // -- MEMORY 事件（每天1个，带FPS） --
                int memFps = 20 + random.nextInt(41); // 20-60
                String memData = "{\"usedMemoryMB\":" + (100 + random.nextInt(400))
                        + ",\"fps\":" + memFps + ",\"totalMemoryMB\":512}";
                LocalDateTime memTime = date.atTime(14 + random.nextInt(8), random.nextInt(60), random.nextInt(60));
                jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                        "MEMORY", memTime.format(DT_FORMAT), memData,
                        versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                count++;

                // -- GPU 事件（每天有50%概率生成） --
                if (random.nextDouble() < 0.5) {
                    int gpuFps = 30 + random.nextInt(31); // 30-60
                    String gpuData = "{\"gpuUsage\":" + (5 + random.nextInt(40))
                            + ",\"fps\":" + gpuFps + ",\"renderTime\":" + (8 + random.nextInt(20)) + "}";
                    LocalDateTime gpuTime = date.atTime(16 + random.nextInt(6), random.nextInt(60), random.nextInt(60));
                    jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                            "GPU", gpuTime.format(DT_FORMAT), gpuData,
                            versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                    count++;
                }

                // -- NETWORK 事件（每天2-4个，约15%失败率；供"按网络"维度统计成功率） --
                int netCount = 2 + random.nextInt(3);
                for (int n = 0; n < netCount; n++) {
                    boolean ok = random.nextDouble() > 0.15;
                    String netData = "{\"duration\":" + (200 + random.nextInt(3000))
                            + ",\"success\":" + ok
                            + ",\"statusCode\":" + (ok ? 200 : 500)
                            + ",\"url\":\"/api/users/profile\"}";
                    LocalDateTime netTime = date.atTime(9 + random.nextInt(12), random.nextInt(60), random.nextInt(60));
                    jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                            "NETWORK", netTime.format(DT_FORMAT), netData,
                            versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                    count++;
                }

                // -- APP_CRASH 事件（每天约20%概率；供"按设备"维度统计崩溃） --
                if (random.nextDouble() < 0.2) {
                    String crashData = "{\"crashType\":\"NullPointerException\""
                            + ",\"stackTrace\":\"at com.example.app.MainActivity.onCreate(MainActivity.java:42)\""
                            + ",\"memoryUsage\":" + (100 + random.nextInt(400)) + "}";
                    LocalDateTime crashTime = date.atTime(11 + random.nextInt(10), random.nextInt(60), random.nextInt(60));
                    jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                            "APP_CRASH", crashTime.format(DT_FORMAT), crashData,
                            versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                    count++;
                }

                // -- ANR 事件（每天约15%概率） --
                if (random.nextDouble() < 0.15) {
                    String anrData = "{\"blockDuration\":" + (5000 + random.nextInt(8000))
                            + ",\"blockThread\":\"main\"}";
                    LocalDateTime anrTime = date.atTime(13 + random.nextInt(8), random.nextInt(60), random.nextInt(60));
                    jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                            "ANR", anrTime.format(DT_FORMAT), anrData,
                            versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                    count++;
                }

                // -- LOW_FPS 卡顿事件（每天约40%概率） --
                if (random.nextDouble() < 0.4) {
                    String lagData = "{\"fps\":" + (10 + random.nextInt(20))
                            + ",\"dropDuration\":" + (1000 + random.nextInt(3000)) + "}";
                    LocalDateTime lagTime = date.atTime(15 + random.nextInt(7), random.nextInt(60), random.nextInt(60));
                    jdbcTemplate.update(sql, "test-app", deviceId, userId, sessionId,
                            "LOW_FPS", lagTime.format(DT_FORMAT), lagData,
                            versionCode, osVersion, pick(NETWORK_TYPES, random), LocalDateTime.now().format(DT_FORMAT));
                    count++;
                }
            }
        }

        log.info("✓ performance_data: {} 条（10设备 × 7天 × 多事件类型）", count);
        return count;
    }

    // ==================== 告警数据 ====================

    private int generateAlertData(LocalDate today) {
        String sql = "INSERT INTO alert_instance " +
                "(app_key, rule_name, rule_id, metric, severity, status, " +
                "current_value, threshold_value, message, start_time, end_time, metric_time, " +
                "assignee, process_note, process_time, create_time, update_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Object[][] alerts = {
                // [ruleName, ruleId, metric, severity, status, currentVal, thresholdVal, message, daysAgo, assignee, processNote]
                {"崩溃率过高", 1L, "crashRate", "HIGH", "FIRING",
                        "5.2", "2.0", "崩溃率为 5.2%，超过阈值 2%", 0, null, null},
                {"启动时间异常", 2L, "startupTime", "MEDIUM", "FIRING",
                        "3200", "2500", "平均启动时间 3200ms，超过阈值 2500ms", 0, null, null},
                {"卡顿率告警", 3L, "lagRate", "HIGH", "FIRING",
                        "1.8", "1.0", "卡顿率 1.8%，超过阈值 1.0%", 1, null, null},
                {"网络失败率过高", 4L, "networkFailureRate", "MEDIUM", "ACKNOWLEDGED",
                        "3.5", "2.0", "网络失败率 3.5%，超过阈值 2.0%", 2, "张三", "正在排查CDN节点问题"},
                {"内存使用率过高", 5L, "memoryUsage", "HIGH", "ACKNOWLEDGED",
                        "85", "80", "内存使用率 85%，超过阈值 80%", 2, "李四", "已定位到图片缓存泄漏"},
                {"CPU使用率异常", 6L, "cpuUsage", "MEDIUM", "RESOLVED",
                        "72", "70", "CPU使用率 72%，超过阈值 70%", 4, "王五", "优化了后台线程调度"},
                {"ANR率告警", 7L, "anrRate", "HIGH", "RESOLVED",
                        "0.8", "0.5", "ANR率 0.8%，超过阈值 0.5%", 5, "赵六", "修复了主线程IO操作"},
                {"页面加载超时", 8L, "pageLoadTime", "LOW", "CLOSED",
                        "4500", "4000", "页面加载时间 4500ms，超过阈值 4000ms", 6, "孙七", "误报-网络波动导致"}
        };

        int count = 0;
        for (Object[] a : alerts) {
            LocalDate alertDate = today.minusDays((Integer) a[8]);
            LocalDateTime startTime = alertDate.atTime(9 + random.nextInt(10), random.nextInt(60), random.nextInt(60));
            LocalDateTime now = LocalDateTime.now();

            jdbcTemplate.update(sql,
                    "test-app",                         // app_key
                    a[0],                               // rule_name
                    a[1],                               // rule_id
                    a[2],                               // metric
                    a[3],                               // severity
                    a[4],                               // status
                    Double.valueOf(a[5].toString()),    // current_value
                    Double.valueOf(a[6].toString()),    // threshold_value
                    a[7],                               // message
                    startTime.format(DT_FORMAT),        // start_time
                    null,                               // end_time
                    startTime.format(DT_FORMAT),        // metric_time
                    a[9],                               // assignee
                    a[10],                              // process_note
                    a[9] != null ? now.format(DT_FORMAT) : null, // process_time
                    startTime.format(DT_FORMAT),        // create_time
                    now.format(DT_FORMAT)               // update_time
            );
            count++;
        }

        log.info("✓ alert_instance: {} 条（FIRING/ACK/RESOLVED/CLOSED 各状态）", count);
        return count;
    }

    // ==================== 问题数据 ====================

    private int generateIssueData(LocalDate today) {
        String sql = "INSERT INTO issue " +
                "(app_key, title, description, type, severity, status, " +
                "assignee_id, reporter_id, stack_trace, reproduce_steps, expected_result, actual_result, " +
                "device_info, occurrence_count, affected_user_count, " +
                "first_occurrence_time, last_occurrence_time, assignee, process_note, process_time, " +
                "create_time, update_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        Object[][] issues = {
                // [title, desc, type, severity, status, stackTrace, occurCount, affectedCount, daysAgo, assignee, processNote]
                {"首页 NullPointerException 崩溃",
                        "进入首页时，Banner 轮播组件加载图片资源时出现空指针异常，导致应用崩溃退出",
                        "CRASH", 4, 0,
                        "java.lang.NullPointerException\n  at com.app.ui.home.BannerAdapter.bindView(BannerAdapter.java:142)\n  at com.app.ui.home.HomeFragment.onViewCreated(HomeFragment.java:89)",
                        156, 120, 0, null, null},

                {"视频播放页面 ANR",
                        "用户进入视频详情页后，主线程在执行视频预加载时阻塞超过5秒，触发ANR",
                        "ANR", 3, 1,
                        "main thread blocked for 5.2s\n  at com.app.video.VideoPreloader.preload(VideoPreloader.java:78)\n  at com.app.video.VideoDetailActivity.onCreate(VideoDetailActivity.java:210)",
                        89, 65, 0, "张三", "正在优化视频预加载逻辑"},

                {"商品列表滚动卡顿",
                        "商品列表页包含大量图片时，RecyclerView 滑动帧率下降至30fps以下，影响用户体验",
                        "PERFORMANCE", 2, 1,
                        null,
                        230, 180, 1, "李四", "已添加图片懒加载和三级缓存"},

                {"支付确认按钮无响应",
                        "用户在支付确认页面点击「确认支付」按钮后无任何反馈，按钮点击事件未正确绑定",
                        "UI", 3, 0,
                        null,
                        45, 38, 1, null, null},

                {"后台内存泄漏导致OOM",
                        "长时间运行后，Activity 未正确释放导致内存泄漏，最终触发 OutOfMemoryError",
                        "CRASH", 4, 2,
                        "java.lang.OutOfMemoryError: Failed to allocate a 32MB allocation\n  at com.app.utils.ImageCache.loadBitmap(ImageCache.java:56)\n  at com.app.ui.BaseActivity.onResume(BaseActivity.java:120)",
                        320, 280, 2, "王五", "已修复ImageCache弱引用改为LRU缓存，Activity泄漏已处理"},

                {"网络请求超时导致白屏",
                        "弱网环境下 API 请求超过10秒仍未返回，页面显示白屏，未做超时处理",
                        "PERFORMANCE", 3, 0,
                        null,
                        178, 150, 3, null, null},

                {"推送消息点击崩溃",
                        "点击系统通知栏推送消息进入特定页面时，Intent 参数解析异常导致崩溃",
                        "CRASH", 3, 2,
                        "java.lang.IllegalArgumentException: Invalid intent data\n  at com.app.push.PushHandler.handleMessage(PushHandler.java:67)",
                        98, 80, 4, "赵六", "增加了DeepLink参数容错处理"},

                {"夜间模式切换卡死",
                        "从设置页面切换夜间模式时，全局主题切换耗时过长，界面卡死约3秒",
                        "ANR", 2, 3,
                        null,
                        62, 55, 5, "孙七", "改为异步切换主题，影响较小已关闭"}
        };

        int count = 0;
        for (Object[] iss : issues) {
            int daysAgo = (Integer) iss[8];
            LocalDateTime firstTime = today.minusDays(daysAgo + 2)
                    .atTime(9 + random.nextInt(10), random.nextInt(60), random.nextInt(60));
            LocalDateTime lastTime = today.minusDays(daysAgo)
                    .atTime(14 + random.nextInt(8), random.nextInt(60), random.nextInt(60));
            LocalDateTime now = LocalDateTime.now();

            // 随机选择设备和地域信息作为 JSON
            int devIdx = random.nextInt(DEVICE_MODELS.length);
            int provIdx = random.nextInt(PROVINCES.length);
            String deviceInfo = "{\"model\":\"" + DEVICE_MODELS[devIdx] + "\","
                    + "\"osVersion\":\"" + OS_VERSIONS[random.nextInt(OS_VERSIONS.length)] + "\","
                    + "\"networkType\":\"" + pick(NETWORK_TYPES, random) + "\","
                    + "\"province\":\"" + PROVINCES[provIdx] + "\","
                    + "\"city\":\"" + CITIES[provIdx][random.nextInt(CITIES[provIdx].length)] + "\"}";

            jdbcTemplate.update(sql,
                    "test-app",                             // app_key
                    iss[0],                                 // title
                    iss[1],                                 // description
                    iss[2],                                 // type
                    iss[3],                                 // severity
                    iss[4],                                 // status
                    1L,                                     // assignee_id
                    2L,                                     // reporter_id
                    iss[5],                                 // stack_trace
                    null,                                   // reproduce_steps
                    null,                                   // expected_result
                    null,                                   // actual_result
                    deviceInfo,                             // device_info
                    iss[6],                                 // occurrence_count
                    iss[7],                                 // affected_user_count
                    firstTime.format(DT_FORMAT),            // first_occurrence_time
                    lastTime.format(DT_FORMAT),             // last_occurrence_time
                    iss[9],                                 // assignee
                    iss[10],                                // process_note
                    iss[9] != null ? now.format(DT_FORMAT) : null, // process_time
                    firstTime.format(DT_FORMAT),            // create_time
                    now.format(DT_FORMAT)                   // update_time
            );
            count++;
        }

        log.info("✓ issue: {} 条（CRASH/ANR/PERFORMANCE/UI 各类型）", count);
        return count;
    }

    // ==================== Redis 在线用户 ====================

    private void writeRedisOnlineUsers() {
        if (redisTemplate == null) {
            log.warn("Redis 不可用，跳过在线用户数写入");
            return;
        }
        try {
            int onlineUsers = 850 + random.nextInt(300); // 850-1150
            redisTemplate.opsForValue().set("online_users:test-app", onlineUsers, 5, TimeUnit.MINUTES);
            log.info("✓ Redis online_users:test-app = {} (5分钟过期)", onlineUsers);
        } catch (Exception e) {
            log.warn("写入 Redis 在线用户数失败: {}", e.getMessage());
        }
    }

    // ==================== 工具方法 ====================

    private static <T> T pick(T[] arr, Random r) {
        return arr[r.nextInt(arr.length)];
    }
}
