package com.monitor.controller;

import com.monitor.common.Result;
import com.monitor.dto.DashboardQueryDTO;
import com.monitor.dto.PerformanceDataReportDTO;
import com.monitor.service.PerformanceDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 性能数据上报控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/performance")
@RequiredArgsConstructor
@Tag(name = "性能数据上报", description = "性能数据上报相关接口")
public class PerformanceDataController {

    private final PerformanceDataService performanceDataService;

    /**
     * 批量上报性能数据
     *
     * @param reportDTO 性能数据上报DTO
     * @return 上报结果
     */
    @PostMapping("/report")
    @Operation(summary = "批量上报性能数据", description = "支持批量上报性能数据，一次最多100条")
    public ResponseEntity<Result<Void>> reportPerformanceData(@Validated @RequestBody PerformanceDataReportDTO reportDTO) {
        log.info("接收到性能数据上报请求，应用：{}，数据条数：{}", reportDTO.getAppKey(), reportDTO.getData().size());

        performanceDataService.batchSavePerformanceData(reportDTO);

        return ResponseEntity.ok(Result.success());
    }

    /**
     * 获取性能趋势数据
     *
     * @param queryDTO 查询参数
     * @return 趋势数据
     */
    @GetMapping("/trend")
    @Operation(summary = "获取性能趋势数据", description = "获取指定应用在指定时间范围内的性能趋势数据")
    public ResponseEntity<Result<Object>> getPerformanceTrend(@Validated DashboardQueryDTO queryDTO) {
        log.info("获取性能趋势数据，应用：{}，时间范围：{} - {}，粒度：{}",
                queryDTO.getAppKey(), queryDTO.getStartTime(), queryDTO.getEndTime(), queryDTO.getGranularity());

        Object trendData = performanceDataService.getPerformanceTrend(queryDTO);

        return ResponseEntity.ok(Result.success(trendData));
    }

    /**
     * 获取地理分布数据
     *
     * @param queryDTO 查询参数
     * @return 地理分布数据
     */
    @GetMapping("/geo")
    @Operation(summary = "获取地理分布数据", description = "获取指定应用在指定时间范围内的用户地理分布数据")
    public ResponseEntity<Result<Object>> getGeoDistribution(@Validated DashboardQueryDTO queryDTO) {
        log.info("获取地理分布数据，应用：{}，时间范围：{} - {}",
                queryDTO.getAppKey(), queryDTO.getStartTime(), queryDTO.getEndTime());

        Object geoData = performanceDataService.getGeoDistribution(queryDTO);

        return ResponseEntity.ok(Result.success(geoData));
    }

    /**
     * 获取版本对比数据
     *
     * @param queryDTO 查询参数
     * @return 版本对比数据
     */
    @GetMapping("/version")
    @Operation(summary = "获取版本对比数据", description = "获取指定应用在指定时间范围内的版本对比数据")
    public ResponseEntity<Result<Object>> getVersionComparison(@Validated DashboardQueryDTO queryDTO) {
        log.info("获取版本对比数据，应用：{}，时间范围：{} - {}",
                queryDTO.getAppKey(), queryDTO.getStartTime(), queryDTO.getEndTime());

        Object versionData = performanceDataService.getVersionComparison(queryDTO);

        return ResponseEntity.ok(Result.success(versionData));
    }

    /**
     * 获取设备维度分布数据
     *
     * @param queryDTO 查询参数
     * @return 设备维度数据
     */
    @GetMapping("/device")
    @Operation(summary = "获取设备维度数据", description = "按设备聚合事件数、崩溃/ANR/卡顿次数与平均启动时间")
    public ResponseEntity<Result<Object>> getDeviceDistribution(@Validated DashboardQueryDTO queryDTO) {
        log.info("获取设备维度数据，应用：{}，时间范围：{} - {}",
                queryDTO.getAppKey(), queryDTO.getStartTime(), queryDTO.getEndTime());

        Object deviceData = performanceDataService.getDeviceDistribution(queryDTO);

        return ResponseEntity.ok(Result.success(deviceData));
    }

    /**
     * 获取网络类型维度分布数据
     *
     * @param queryDTO 查询参数
     * @return 网络维度数据
     */
    @GetMapping("/network")
    @Operation(summary = "获取网络维度数据", description = "按网络类型聚合用户数、失败次数与平均启动时间")
    public ResponseEntity<Result<Object>> getNetworkDistribution(@Validated DashboardQueryDTO queryDTO) {
        log.info("获取网络维度数据，应用：{}，时间范围：{} - {}",
                queryDTO.getAppKey(), queryDTO.getStartTime(), queryDTO.getEndTime());

        Object networkData = performanceDataService.getNetworkDistribution(queryDTO);

        return ResponseEntity.ok(Result.success(networkData));
    }

    /**
     * 获取FPS分布数据
     *
     * @param queryDTO 查询参数
     * @return FPS分布数据
     */
    @GetMapping("/fps")
    @Operation(summary = "获取FPS分布数据", description = "获取指定应用在指定时间范围内的FPS分布数据")
    public ResponseEntity<Result<Object>> getFpsDistribution(@Validated DashboardQueryDTO queryDTO) {
        log.info("获取FPS分布数据，应用：{}，时间范围：{} - {}",
                queryDTO.getAppKey(), queryDTO.getStartTime(), queryDTO.getEndTime());

        Object fpsData = performanceDataService.getFpsDistribution(queryDTO);

        return ResponseEntity.ok(Result.success(fpsData));
    }


}
