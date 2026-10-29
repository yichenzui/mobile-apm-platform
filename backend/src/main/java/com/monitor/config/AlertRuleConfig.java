package com.monitor.config;

import org.springframework.context.annotation.Configuration;

import com.monitor.service.impl.AlertRuleEngineImpl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;


/**
 * 告警规则配置类
 */
@Slf4j
@Configuration
public class AlertRuleConfig {

    private final AlertRuleEngineImpl alertRuleEngine;

    public AlertRuleConfig(AlertRuleEngineImpl alertRuleEngine) {
        this.alertRuleEngine = alertRuleEngine;
    }

    /**
     * 初始化告警规则引擎
     */
    @PostConstruct
    public void initAlertRuleEngine() {
        alertRuleEngine.initEvaluators();
        log.info("告警规则引擎初始化完成");
    }
}
