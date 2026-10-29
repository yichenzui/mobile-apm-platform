package com.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.monitor.entity.AlertRule;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;

/**
 * 告警规则 Mapper
 *
 * 用于数据聚合后的告警检测：按应用查询已启用的规则。
 */
@Mapper
public interface AlertRuleMapper extends BaseMapper<AlertRule> {

    /**
     * 查询某应用下所有已启用且未删除的告警规则
     */
    default List<AlertRule> selectEnabledByAppKey(String appKey) {
        return selectListByQuery(QueryWrapper.create()
                .where("app_key = ?", appKey)
                .and("enabled = 1")
                .and("(deleted = 0 OR deleted IS NULL)"));
    }

    /**
     * 查询全部已启用且未删除的告警规则（定时任务全量巡检用）
     */
    default List<AlertRule> selectAllEnabled() {
        return selectListByQuery(QueryWrapper.create()
                .where("enabled = 1")
                .and("(deleted = 0 OR deleted IS NULL)"));
    }
}
