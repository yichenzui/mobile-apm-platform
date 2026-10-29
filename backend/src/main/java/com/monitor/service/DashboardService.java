package com.monitor.service;

import com.monitor.dto.DashboardQueryDTO;
import com.monitor.vo.DashboardSummaryVO;

/**
 * 看板服务接口
 */
public interface DashboardService {

    /**
     * 获取看板汇总数据
     *
     * @param queryDTO 查询参数
     * @return 看板汇总数据
     */
    DashboardSummaryVO getDashboardSummary(DashboardQueryDTO queryDTO);
}
