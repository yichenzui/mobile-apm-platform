package com.monitor.service;

import com.monitor.entity.AlertInstance;
import com.monitor.vo.AlertRecordVO;

/**
 * 告警服务接口
 */
public interface AlertService {

    /**
     * 发送告警通知
     *
     * @param alertInstance 告警实例
     */
    void sendAlertNotification(AlertInstance alertInstance);

    /**
     * 获取告警列表
     *
     * @param queryDTO 查询参数
     * @return 告警列表
     */
    java.util.List<AlertRecordVO> getAlertList(com.monitor.dto.AlertQueryDTO queryDTO);

    /**
     * 统计符合条件的告警总数（用于分页）
     *
     * @param queryDTO 查询参数
     * @return 总条数
     */
    int countAlerts(com.monitor.dto.AlertQueryDTO queryDTO);

    /**
     * 获取告警详情
     *
     * @param alertId 告警ID
     * @return 告警详情
     */
    AlertRecordVO getAlertDetail(Long alertId);

    /**
     * 确认告警
     *
     * @param alertId 告警ID
     * @param assignee 处理人
     * @param note 处理备注
     */
    void acknowledgeAlert(Long alertId, String assignee, String note);

    /**
     * 解决告警
     *
     * @param alertId 告警ID
     * @param assignee 处理人
     * @param note 处理备注
     */
    void resolveAlert(Long alertId, String assignee, String note);

    /**
     * 关闭告警
     *
     * @param alertId 告警ID
     * @param assignee 处理人
     * @param note 处理备注
     */
    void closeAlert(Long alertId, String assignee, String note);
}
