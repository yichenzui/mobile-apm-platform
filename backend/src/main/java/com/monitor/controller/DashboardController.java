package com.monitor.controller;

import com.monitor.common.Result;
import com.monitor.dto.DashboardQueryDTO;
import com.monitor.vo.DashboardSummaryVO;
import com.monitor.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 看板数据控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "实时看板", description = "实时看板数据相关接口")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取实时性能看板数据
     *
     * @param queryDTO 查询参数
     * @return 看板数据
     */
    @GetMapping("/summary")
    @Operation(summary = "获取实时性能看板数据", description = "获取指定应用在指定时间范围内的性能汇总数据")
    public ResponseEntity<Result<DashboardSummaryVO>> getDashboardSummary(@Validated DashboardQueryDTO queryDTO) {
        log.info("获取看板数据，应用：{}，时间范围：{} - {}，粒度：{}", 
                queryDTO.getAppKey(), queryDTO.getStartTime(), queryDTO.getEndTime(), queryDTO.getGranularity());

        DashboardSummaryVO summary = dashboardService.getDashboardSummary(queryDTO);

        return ResponseEntity.ok(Result.success(summary));
    }
}
