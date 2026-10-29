package com.monitor.service;

import com.monitor.entity.AlertRule;
import com.monitor.entity.AlertInstance;
import com.monitor.dto.MetricDataDTO;

import java.util.List;

/**
 * 告警规则引擎接口
 */
public interface AlertRuleEngine {

    /**
     * 评估指标数据是否触发告警
     *
     * @param rule 告警规则
     * @param metricData 指标数据
     * @return 是否触发告警
     */
    boolean evaluateRule(AlertRule rule, MetricDataDTO metricData);

    /**
     * 评估指标数据是否触发告警（批量）
     *
     * @param rules 告警规则列表
     * @param metricDataList 指标数据列表
     * @return 触发的告警实例列表
     */
    List<AlertInstance> evaluateRules(List<AlertRule> rules, List<MetricDataDTO> metricDataList);

    /**
     * 注册规则评估器
     *
     * @param ruleType 规则类型
     * @param evaluator 评估器
     */
    void registerEvaluator(AlertRule.RuleType ruleType, RuleEvaluator evaluator);

    /**
     * 获取规则评估器
     *
     * @param ruleType 规则类型
     * @return 评估器
     */
    RuleEvaluator getEvaluator(AlertRule.RuleType ruleType);

    /**
     * 规则评估器接口
     */
    interface RuleEvaluator {
        /**
         * 评估规则是否触发
         *
         * @param rule 告警规则
         * @param metricData 指标数据
         * @return 是否触发告警
         */
        boolean evaluate(AlertRule rule, MetricDataDTO metricData);
    }
}
