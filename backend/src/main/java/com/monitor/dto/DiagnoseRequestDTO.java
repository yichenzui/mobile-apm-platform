package com.monitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 智能诊断请求 DTO
 */
@Data
@Schema(description = "AI 智能诊断请求")
public class DiagnoseRequestDTO {

    @NotBlank(message = "应用Key不能为空")
    @Schema(description = "应用标识", example = "test-app")
    private String appKey;

    @Schema(description = "开始时间", example = "2026-06-10T00:00:00")
    private String startTime;

    @Schema(description = "结束时间", example = "2026-06-17T23:59:59")
    private String endTime;

    @Schema(description = "诊断维度：crash/startup/lag/network/all", example = "all")
    private String dimension = "all";
}
