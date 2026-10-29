package com.monitor.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitor.dto.MetricDataDTO;
import com.monitor.entity.AlertInstance;
import com.monitor.entity.AlertRule;
import com.monitor.service.AlertRuleEngine;
import com.monitor.service.AlertService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 告警规则引擎实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertRuleEngineImpl implements AlertRuleEngine, InitializingBean {

    private final ObjectMapper objectMapper;
    private final AlertService alertService;
    private final Map<AlertRule.RuleType, RuleEvaluator> evaluators = new HashMap<>();

    @Override
    public boolean evaluateRule(AlertRule rule, MetricDataDTO metricData) {
        if (rule == null || metricData == null || rule.getEnabled() != 1) {
            return false;
        }

        RuleEvaluator evaluator = getEvaluator(rule.getRuleType());
        if (evaluator == null) {
            log.warn("未找到规则类型 {} 的评估器", rule.getRuleType());
            return false;
        }

        return evaluator.evaluate(rule, metricData);
    }

    @Override
    public List<AlertInstance> evaluateRules(List<AlertRule> rules, List<MetricDataDTO> metricDataList) {
        List<AlertInstance> alertInstances = new ArrayList<>();

        for (MetricDataDTO metricData : metricDataList) {
            for (AlertRule rule : rules) {
                if (evaluateRule(rule, metricData)) {
                    // 创建告警实例
                    AlertInstance alertInstance = createAlertInstance(rule, metricData);
                    alertInstances.add(alertInstance);

                    // 发送告警通知
                    alertService.sendAlertNotification(alertInstance);
                }
            }
        }

        return alertInstances;
    }

    @Override
    public void registerEvaluator(AlertRule.RuleType ruleType, RuleEvaluator evaluator) {
        evaluators.put(ruleType, evaluator);
        log.info("注册规则评估器: {}", ruleType);
    }

    @Override
    public RuleEvaluator getEvaluator(AlertRule.RuleType ruleType) {
        return evaluators.get(ruleType);
    }

    /**
     * 创建告警实例
     */
    private AlertInstance createAlertInstance(AlertRule rule, MetricDataDTO metricData) {
        AlertInstance alertInstance = new AlertInstance();
        alertInstance.setRuleId(rule.getId());
        alertInstance.setRuleName(rule.getRuleName());
        alertInstance.setAppKey(rule.getAppKey());
        alertInstance.setMetric(rule.getMetric());
        alertInstance.setSeverity(rule.getSeverity());
        Number val = metricData.getValue();
        java.math.BigDecimal bd = null;
        if (val != null) {
            bd = new java.math.BigDecimal(val.toString());
        }
        alertInstance.setCurrentValue(bd);
        alertInstance.setThresholdValue(getThresholdValue(rule));
        alertInstance.setStatus(AlertInstance.AlertStatus.FIRING);
        alertInstance.setStartTime(LocalDateTime.now());
        alertInstance.setMetricTime(metricData.getTimestamp());
        // 继承规则的通知渠道，缺省时使用 email，否则通知服务会因 notifyChannels 为 null 而静默失败
        String channels = rule.getNotificationChannels();
        alertInstance.setNotifyChannels(
                (channels == null || channels.isBlank()) ? "[\"email\"]" : channels);

        return alertInstance;
    }

    /**
     * 获取阈值
     */
    private String getThresholdValue(AlertRule rule) {
        try {
            JsonNode config = objectMapper.readTree(rule.getConditionConfig());
            return config.get("threshold").asText();
        } catch (Exception e) {
            log.error("解析规则配置失败", e);
            return "未知";
        }
    }

    /**
     * 初始化评估器
     */
    public void initEvaluators() {
        // 注册阈值规则评估器
        registerEvaluator(AlertRule.RuleType.THRESHOLD, new ThresholdEvaluator());

        // 注册同比规则评估器
        registerEvaluator(AlertRule.RuleType.YOY, new YoyEvaluator());

        // 注册环比规则评估器
        registerEvaluator(AlertRule.RuleType.MOM, new MomEvaluator());

        // 注册异常检测评估器
        registerEvaluator(AlertRule.RuleType.ANOMALY, new AnomalyEvaluator());
    }

    /**
     * 阈值规则评估器
     */
    private static class ThresholdEvaluator implements RuleEvaluator {
        @Override
        public boolean evaluate(AlertRule rule, MetricDataDTO metricData) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode config = mapper.readTree(rule.getConditionConfig());

                String operator = config.get("operator").asText();
                double threshold = config.get("threshold").asDouble();
                double value = metricData.getValue().doubleValue();

                switch (operator) {
                    case "gt": return value > threshold;
                    case "gte": return value >= threshold;
                    case "lt": return value < threshold;
                    case "lte": return value <= threshold;
                    case "eq": return value == threshold;
                    case "ne": return value != threshold;
                    default: return false;
                }
            } catch (Exception e) {
                log.error("阈值规则评估失败", e);
                return false;
            }
        }
    }

    /**
     * 同比规则评估器
     */
    private static class YoyEvaluator implements RuleEvaluator {
        @Override
        public boolean evaluate(AlertRule rule, MetricDataDTO metricData) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode config = mapper.readTree(rule.getConditionConfig());

                String operator = config.get("operator").asText();
                double threshold = config.get("threshold").asDouble();

                // 获取去年同期数据
                MetricDataDTO yoyData = getYoyData(metricData);
                if (yoyData == null) {
                    log.warn("无法获取去年同期数据");
                    return false;
                }

                double currentValue = metricData.getValue().doubleValue();
                double yoyValue = yoyData.getValue().doubleValue();
                // 基期为 0 时直接除会得到 NaN，导致后续所有比较恒为 false、规则静默失效
                double changeRate = yoyValue == 0
                        ? (currentValue == 0 ? 0.0 : 100.0)
                        : (currentValue - yoyValue) / yoyValue * 100;

                switch (operator) {
                    case "gt": return changeRate > threshold;
                    case "gte": return changeRate >= threshold;
                    case "lt": return changeRate < threshold;
                    case "lte": return changeRate <= threshold;
                    case "eq": return Math.abs(changeRate - threshold) < 0.01;
                    case "ne": return Math.abs(changeRate - threshold) >= 0.01;
                    default: return false;
                }
            } catch (Exception e) {
                log.error("同比规则评估失败", e);
                return false;
            }
        }

        private MetricDataDTO getYoyData(MetricDataDTO metricData) {
            // 实现获取去年同期数据的逻辑
            // 这里只是模拟实现，实际应该从数据库查询
            try {
                MetricDataDTO yoyData = new MetricDataDTO();
                yoyData.setAppKey(metricData.getAppKey());
                yoyData.setMetric(metricData.getMetric());
                yoyData.setTimestamp(metricData.getTimestamp().minusYears(1));
                // 模拟去年同期数据，实际应该从数据库查询
                yoyData.setValue(metricData.getValue().doubleValue() * 0.9); // 假设去年同期是当前值的90%
                return yoyData;
            } catch (Exception e) {
                log.error("获取去年同期数据失败", e);
                return null;
            }
        }
    }

    /**
     * 环比规则评估器
     */
    private static class MomEvaluator implements RuleEvaluator {
        @Override
        public boolean evaluate(AlertRule rule, MetricDataDTO metricData) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode config = mapper.readTree(rule.getConditionConfig());

                String operator = config.get("operator").asText();
                double threshold = config.get("threshold").asDouble();

                // 获取上一周期数据
                MetricDataDTO momData = getMomData(metricData);
                if (momData == null) {
                    log.warn("无法获取上一周期数据");
                    return false;
                }

                double currentValue = metricData.getValue().doubleValue();
                double momValue = momData.getValue().doubleValue();
                // 同上：避免基期为 0 时除出 NaN
                double changeRate = momValue == 0
                        ? (currentValue == 0 ? 0.0 : 100.0)
                        : (currentValue - momValue) / momValue * 100;

                switch (operator) {
                    case "gt": return changeRate > threshold;
                    case "gte": return changeRate >= threshold;
                    case "lt": return changeRate < threshold;
                    case "lte": return changeRate <= threshold;
                    case "eq": return Math.abs(changeRate - threshold) < 0.01;
                    case "ne": return Math.abs(changeRate - threshold) >= 0.01;
                    default: return false;
                }
            } catch (Exception e) {
                log.error("环比规则评估失败", e);
                return false;
            }
        }

        private MetricDataDTO getMomData(MetricDataDTO metricData) {
            // 实现获取上一周期数据的逻辑
            // 这里只是模拟实现，实际应该从数据库查询
            try {
                MetricDataDTO momData = new MetricDataDTO();
                momData.setAppKey(metricData.getAppKey());
                momData.setMetric(metricData.getMetric());
                // 假设周期为1小时，实际应该根据规则配置确定周期
                momData.setTimestamp(metricData.getTimestamp().minusHours(1));
                // 模拟上一周期数据，实际应该从数据库查询
                momData.setValue(metricData.getValue().doubleValue() * 0.95); // 假设上一周期是当前值的95%
                return momData;
            } catch (Exception e) {
                log.error("获取上一周期数据失败", e);
                return null;
            }
        }
    }

    /**
     * 异常检测评估器
     */
    private static class AnomalyEvaluator implements RuleEvaluator {
        @Override
        public boolean evaluate(AlertRule rule, MetricDataDTO metricData) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode config = mapper.readTree(rule.getConditionConfig());

                double sigma = config.get("sigma").asDouble(3.0); // 默认3-sigma

                // 获取历史数据
                List<MetricDataDTO> historyData = getHistoryData(metricData);
                if (historyData == null || historyData.size() < 30) {
                    log.warn("历史数据不足，无法进行异常检测");
                    return false;
                }

                // 计算均值和标准差
                double mean = calculateMean(historyData);
                double stdDev = calculateStdDev(historyData, mean);

                // 检测是否为异常值（标准差为 0 说明历史数据无波动，视为非异常，避免除出 Infinity/NaN）
                double currentValue = metricData.getValue().doubleValue();
                double zScore = stdDev <= 1e-9 ? 0.0 : Math.abs((currentValue - mean) / stdDev);

                return zScore > sigma;
            } catch (Exception e) {
                log.error("异常检测规则评估失败", e);
                return false;
            }
        }

        private List<MetricDataDTO> getHistoryData(MetricDataDTO metricData) {
            // 实现获取历史数据的逻辑
            // 这里只是模拟实现，实际应该从数据库查询
            try {
                List<MetricDataDTO> historyData = new ArrayList<>();
                // 模拟30天的历史数据，实际应该从数据库查询
                for (int i = 1; i <= 30; i++) {
                    MetricDataDTO data = new MetricDataDTO();
                    data.setAppKey(metricData.getAppKey());
                    data.setMetric(metricData.getMetric());
                    data.setTimestamp(metricData.getTimestamp().minusDays(i));
                    // 模拟历史数据，实际应该从数据库查询
                    data.setValue(metricData.getValue().doubleValue() * (0.8 + Math.random() * 0.4)); // 80%-120%的随机值
                    historyData.add(data);
                }
                return historyData;
            } catch (Exception e) {
                log.error("获取历史数据失败", e);
                return null;
            }
        }

        private double calculateMean(List<MetricDataDTO> data) {
            return data.stream()
                .mapToDouble(d -> d.getValue().doubleValue())
                .average()
                .orElse(0.0);
        }

        private double calculateStdDev(List<MetricDataDTO> data, double mean) {
            double sumOfSquares = data.stream()
                .mapToDouble(d -> Math.pow(d.getValue().doubleValue() - mean, 2))
                .sum();
            return Math.sqrt(sumOfSquares / data.size());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化评估器
        initEvaluators();
    }
}
