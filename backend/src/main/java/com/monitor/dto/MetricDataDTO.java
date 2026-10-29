package com.monitor.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 指标数据DTO
 */
@Data
public class MetricDataDTO {

    /**
     * 应用标识
     */
    private String appKey;

    /**
     * 指标名称
     */
    private String metric;

    /**
     * 指标值
     */
    private Number value;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 维度信息（如：版本、设备型号等）
     */
    private String dimensions;

    /**
     * 附加信息
     */
    private String extras;
}
