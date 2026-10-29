package com.monitor.controller;

import com.monitor.common.Result;
import com.monitor.dto.IssueQueryDTO;
import com.monitor.vo.IssueVO;
import com.monitor.service.IssueService;
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
 * 问题控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/issue")
@RequiredArgsConstructor
@Tag(name = "问题管理", description = "问题相关接口")
public class IssueController {

    private final IssueService issueService;

    /**
     * 获取问题列表
     *
     * @param queryDTO 查询参数
     * @return 问题列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取问题列表", description = "获取指定应用的问题列表（含分页总数）")
    public ResponseEntity<Result<Map<String, Object>>> getIssueList(@Validated IssueQueryDTO queryDTO) {
        log.info("获取问题列表，应用：{}，页码：{}，页大小：{}",
                queryDTO.getAppKey(), queryDTO.getPageNum(), queryDTO.getPageSize());

        List<IssueVO> issueList = issueService.getIssueList(queryDTO);
        int total = issueService.countIssues(queryDTO);

        // 返回 { records, total, pageNum, pageSize }，前端分页器据此渲染
        Map<String, Object> page = new HashMap<>();
        page.put("records", issueList);
        page.put("total", total);
        page.put("pageNum", queryDTO.getPageNum());
        page.put("pageSize", queryDTO.getPageSize());

        return ResponseEntity.ok(Result.success(page));
    }

    /**
     * 获取问题详情
     *
     * @param id 问题ID
     * @return 问题详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取问题详情", description = "根据ID获取问题详情")
    public ResponseEntity<Result<IssueVO>> getIssueDetail(@PathVariable Long id) {
        log.info("获取问题详情，ID：{}", id);

        IssueVO issueDetail = issueService.getIssueDetail(id);

        return ResponseEntity.ok(Result.success(issueDetail));
    }

    /**
     * 处理问题
     *
     * @param id 问题ID
     * @param note 处理说明
     * @return 操作结果
     */
    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "处理问题", description = "处理问题并添加处理说明")
    public ResponseEntity<Result<Void>> processIssue(@PathVariable Long id, @RequestBody Map<String, String> request) {
        log.info("处理问题，ID：{}，处理人：{}", id, request.get("assignee"));

        String note = request.get("note");
        issueService.processIssue(id, request.get("assignee"), note);

        return ResponseEntity.ok(Result.success());
    }

    /**
     * 解决问题
     *
     * @param id 问题ID
     * @param note 解决方案
     * @return 操作结果
     */
    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "解决问题", description = "解决问题并添加解决方案")
    public ResponseEntity<Result<Void>> resolveIssue(@PathVariable Long id, @RequestBody Map<String, String> request) {
        log.info("解决问题，ID：{}，处理人：{}", id, request.get("assignee"));

        String note = request.get("note");
        issueService.resolveIssue(id, request.get("assignee"), note);

        return ResponseEntity.ok(Result.success());
    }
}
