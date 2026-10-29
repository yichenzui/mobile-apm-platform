package com.monitor.service;

import com.monitor.dto.ChatRequestDTO;
import com.monitor.dto.ChatResponseVO;
import com.monitor.dto.DiagnoseRequestDTO;
import com.monitor.dto.DiagnoseResponseVO;

/**
 * AI 助手服务接口
 */
public interface AiService {

    /**
     * 与 DeepSeek 对话
     * @param request 对话请求
     * @return AI 回复
     */
    ChatResponseVO chat(ChatRequestDTO request);

    /**
     * AI 一键智能诊断
     * 拉取真实性能数据，调用 DeepSeek 生成诊断报告
     * @param request 诊断请求（appKey + 时间范围 + 维度）
     * @return 诊断报告
     */
    DiagnoseResponseVO diagnose(DiagnoseRequestDTO request);
}
