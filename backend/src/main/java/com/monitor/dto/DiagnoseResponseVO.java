package com.monitor.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI 智能诊断响应 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI 智能诊断响应")
public class DiagnoseResponseVO {

    @Schema(description = "诊断报告内容（Markdown格式）")
    private String report;

    @Schema(description = "关键指标摘要")
    private KeyMetrics keyMetrics;

    @Schema(description = "风险等级：LOW/MEDIUM/HIGH/CRITICAL")
    private String riskLevel;

    @Schema(description = "优化建议列表")
    private List<Suggestion> suggestions;

    @Schema(description = "使用的AI模型")
    private String model;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyMetrics {
        private Double crashRate;
        private Double avgStartupTime;
        private Double lagRate;
        private Double networkSuccessRate;
        private Integer activeUsers;
        private Integer alertCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Suggestion {
        private String category;
        private String title;
        private String description;
        private String priority;
    }
}
