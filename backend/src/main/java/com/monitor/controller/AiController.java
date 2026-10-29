package com.monitor.controller;

import com.monitor.common.Result;
import com.monitor.dto.ChatRequestDTO;
import com.monitor.dto.ChatResponseVO;
import com.monitor.dto.DiagnoseRequestDTO;
import com.monitor.dto.DiagnoseResponseVO;
import com.monitor.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * AI 智能助手控制器
 */
@RestController
@RequestMapping("/ai")
@Tag(name = "AI 智能助手", description = "DeepSeek AI 对话与智能诊断接口")
public class AiController {

    @Autowired
    private AiService aiService;

    /**
     * AI 对话
     */
    @PostMapping("/chat")
    @Operation(summary = "AI 对话", description = "发送消息给 DeepSeek AI 获取智能回复")
    public Result<ChatResponseVO> chat(@Valid @RequestBody ChatRequestDTO request) {
        ChatResponseVO response = aiService.chat(request);
        return Result.success(response);
    }

    /**
     * AI 一键智能诊断
     * 拉取指定应用的真实性能数据，调用 DeepSeek 生成结构化诊断报告
     */
    @PostMapping("/diagnose")
    @Operation(summary = "AI 智能诊断", description = "一键诊断应用性能问题，基于真实数据生成结构化诊断报告和优化建议")
    public Result<DiagnoseResponseVO> diagnose(@Valid @RequestBody DiagnoseRequestDTO request) {
        DiagnoseResponseVO response = aiService.diagnose(request);
        return Result.success(response);
    }
}
