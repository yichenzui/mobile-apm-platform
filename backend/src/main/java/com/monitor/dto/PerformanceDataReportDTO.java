package com.monitor.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 性能数据上报DTO
 */
@Data
public class PerformanceDataReportDTO {

    /**
     * 应用标识
     */
    @NotBlank(message = "应用标识不能为空")
    private String appKey;

    /**
     * 性能数据列表
     */
    @NotEmpty(message = "性能数据不能为空")
    @Size(max = 100, message = "一次最多上报100条数据")
    @Valid
    private List<PerformanceDataItem> data;

    /**
     * 性能数据项
     */
    @Data
    public static class PerformanceDataItem {
        /**
         * 事件类型：startup/crash/anr/fps/memory/network
         */
        @NotBlank(message = "事件类型不能为空")
        private String eventType;

        /**
         * 事件发生时间
         */
        @NotNull(message = "事件时间不能为空")
        private LocalDateTime eventTime;

        /**
         * 设备ID
         */
        @NotBlank(message = "设备ID不能为空")
        private String deviceId;

        /**
         * 会话ID
         */
        @NotBlank(message = "会话ID不能为空")
        private String sessionId;

        /**
         * 事件数据（JSON格式）
         */
        @NotNull(message = "事件数据不能为空")
        private JsonNode data;

        /**
         * 附加信息
         */
        private JsonNode extras;
    }
}
