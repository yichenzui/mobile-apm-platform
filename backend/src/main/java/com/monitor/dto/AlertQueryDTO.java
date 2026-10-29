package com.monitor.dto;

import lombok.Data;

@Data
public class AlertQueryDTO {

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 页大小
     */
    private Integer pageSize = 10;

    /**
     * 告警严重程度: CRITICAL/WARNING/INFO
     */
    private String severity;

    /**
     * 告警状态: FIRING/ACKNOWLEDGED/RESOLVED/CLOSED
     */
    private String status;

    /**
     * 分页偏移量（由 pageNum/pageSize 计算，供 SQL 的 OFFSET 使用）
     */
    public Integer getOffset() {
        int num = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        int size = (pageSize == null || pageSize < 1) ? 10 : pageSize;
        return (num - 1) * size;
    }
}
