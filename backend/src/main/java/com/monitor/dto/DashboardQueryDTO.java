package com.monitor.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 看板查询DTO
 */
@Data
public class DashboardQueryDTO {

    /**
     * 应用标识
     */
    @NotBlank(message = "应用标识不能为空")
    private String appKey;

    /**
     * 开始时间
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 时间粒度：hour/day
     */
    @NotBlank(message = "时间粒度不能为空")
    private String granularity;
}
