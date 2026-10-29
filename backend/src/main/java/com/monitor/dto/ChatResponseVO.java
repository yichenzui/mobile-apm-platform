package com.monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 对话响应 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseVO {

    /** AI 回复内容 */
    private String content;

    /** 模型名称 */
    private String model;

    /** Token 使用量（输入） */
    private int promptTokens;

    /** Token 使用量（输出） */
    private int completionTokens;
}
