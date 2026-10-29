package com.monitor.controller;

import com.monitor.common.Result;
import com.monitor.dto.AlertQueryDTO;
import com.monitor.vo.AlertRecordVO;
import com.monitor.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 告警控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/alert")
@RequiredArgsConstructor
@Tag(name = "告警管理", description = "告警相关接口")
public class AlertController {

    private final AlertService alertService;


    


    /**
     * 获取告警列表
     *
     * @param queryDTO 查询参数
     * @return 告警列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取告警列表", description = "获取指定应用的告警列表（含分页总数）")
    public ResponseEntity<Result<Map<String, Object>>> getAlertList(@Validated AlertQueryDTO queryDTO) {
        log.info("获取告警列表，应用：{}，页码：{}，页大小：{}",
                queryDTO.getAppKey(), queryDTO.getPageNum(), queryDTO.getPageSize());

        List<AlertRecordVO> alertList = alertService.getAlertList(queryDTO);
        int total = alertService.countAlerts(queryDTO);

        // 返回 { records, total, pageNum, pageSize }，前端分页器据此渲染
        Map<String, Object> page = new HashMap<>();
        page.put("records", alertList);
        page.put("total", total);
        page.put("pageNum", queryDTO.getPageNum());
        page.put("pageSize", queryDTO.getPageSize());

        return ResponseEntity.ok(Result.success(page));
    }

    /**
     * 获取告警详情
     *
     * @param id 告警ID
     * @return 告警详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取告警详情", description = "根据ID获取告警详情")
    public ResponseEntity<Result<AlertRecordVO>> getAlertDetail(@PathVariable Long id) {
        log.info("获取告警详情，ID：{}", id);

        AlertRecordVO alertDetail = alertService.getAlertDetail(id);

        return ResponseEntity.ok(Result.success(alertDetail));
    }

    /**
     * 确认告警
     *
     * @param id 告警ID
     * @param note 处理说明
     * @return 操作结果
     */
    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "确认告警", description = "确认告警并添加处理说明")
    public ResponseEntity<Result<Void>> acknowledgeAlert(@PathVariable Long id, @RequestBody Map<String, String> request) {
        log.info("确认告警，ID：{}，处理人：{}", id, request.get("assignee"));

        String note = request.get("note");
        alertService.acknowledgeAlert(id, request.get("assignee"), note);

        return ResponseEntity.ok(Result.success());
    }

    /**
     * 解决告警
     *
     * @param id 告警ID
     * @param note 解决方案
     * @return 操作结果
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "解决告警", description = "解决告警并添加解决方案")
    public ResponseEntity<Result<Void>> resolveAlert(@PathVariable Long id, @RequestBody Map<String, String> request) {
        log.info("解决告警，ID：{}，处理人：{}", id, request.get("assignee"));

        String note = request.get("note");
        alertService.resolveAlert(id, request.get("assignee"), note);

        return ResponseEntity.ok(Result.success());
    }
}
