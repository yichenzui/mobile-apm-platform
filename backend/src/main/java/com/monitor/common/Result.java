package com.monitor.common;

import lombok.Data;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> {

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail() {
        return new Result<>(500, "操作失败", null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(500, message, null);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
