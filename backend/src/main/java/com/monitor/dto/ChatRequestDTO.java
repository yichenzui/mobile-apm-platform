package com.monitor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * AI 对话请求 DTO
 */
@Data
public class ChatRequestDTO {

    /** 用户消息内容 */
    @NotBlank(message = "消息内容不能为空")
    @Size(max = 4096, message = "消息长度不能超过4096字符")
    private String content;

    /** 对话历史（用于多轮对话） */
    private List<Message> messages;

    /**
     * 消息记录
     */
    @Data
    public static class Message {
        private String role;  // user / assistant
        private String content;
    }
}
