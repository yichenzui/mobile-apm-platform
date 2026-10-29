package com.monitor.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monitor.common.Result;
import com.monitor.entity.Application;
import com.monitor.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 应用管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/application")
@RequiredArgsConstructor
@Tag(name = "应用管理", description = "应用管理相关接口")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 获取应用列表
     *
     * @return 应用列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取应用列表", description = "获取所有可用的应用列表")
    public ResponseEntity<Result<List<Application>>> getApplicationList() {
        log.info("获取应用列表");

        List<Application> applications = applicationService.getAllApplications();

        return ResponseEntity.ok(Result.success(applications));
    }

    /**
     * 根据ID获取应用
     *
     * @param id 应用ID
     * @return 应用对象
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取应用", description = "根据ID获取应用详情")
    public ResponseEntity<Result<Application>> getApplicationById(@PathVariable Integer id) {
        log.info("根据ID获取应用: {}", id);

        Application application = applicationService.getApplicationById(Long.valueOf(id));

        return ResponseEntity.ok(Result.success(application));
    }

    /**
     * 创建应用
     *
     * @param application 应用对象
     * @return 创建的应用对象
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建应用", description = "创建新的应用")
    public ResponseEntity<Result<Application>> createApplication(@RequestBody Application application) {
        log.info("创建应用: {}", application.getName());

        Application createdApplication = applicationService.createApplication(application);

        return ResponseEntity.ok(Result.success(createdApplication));
    }

    /**
     * 更新应用
     *
     * @param id 应用ID
     * @param application 应用对象
     * @return 更新后的应用对象
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新应用", description = "更新应用信息")
    public ResponseEntity<Result<Application>> updateApplication(@PathVariable Integer id, @RequestBody Application application) {
        log.info("更新应用: {}", id);

        Application updatedApplication = applicationService.updateApplication(Long.valueOf(id), application);

        return ResponseEntity.ok(Result.success(updatedApplication));
    }

    /**
     * 删除应用
     *
     * @param id 应用ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除应用", description = "删除应用")
    public ResponseEntity<Result<Void>> deleteApplication(@PathVariable Integer id) {
        log.info("删除应用: {}", id);

        applicationService.deleteApplication(Long.valueOf(id));

        return ResponseEntity.ok(Result.success());
    }
}
