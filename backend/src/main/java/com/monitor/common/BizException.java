package com.monitor.common;

/**
 * 业务异常
 *
 * 与 RuntimeException 的区别：
 * - BizException 的 message 是"可以安全展示给最终用户"的业务提示（如"原密码错误"、"用户名已存在"）；
 * - 普通 RuntimeException 通常是空指针、SQL 错误等内部问题，message 不能直接回给前端。
 *
 * 全局异常处理器对 BizException 保留原文，对其余异常只返回通用文案（原文仅写入日志）。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
