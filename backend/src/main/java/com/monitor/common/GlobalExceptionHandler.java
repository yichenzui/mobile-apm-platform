package com.monitor.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求体读取异常（JSON格式错误等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.fail(400, "请求数据格式错误，请检查JSON格式是否正确");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验失败: {}", message);
        return Result.fail(400, message);
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 处理认证凭证错误（密码错误等）
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: {}", e.getMessage());
        return Result.fail(401, "用户名或密码错误");
    }

    /**
     * 处理业务异常（原密码错误、用户名已存在等）
     * message 是面向用户的提示，原样返回
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(400, e.getMessage());
    }

    /**
     * 处理权限不足（@PreAuthorize 校验失败）
     * 必须排在 RuntimeException 之前，否则会被吞成 400
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return Result.fail(403, "没有权限执行该操作，请联系管理员");
    }

    /**
     * 处理 GET 参数绑定/校验失败（@Validated 修饰的 @ModelAttribute 查询对象）
     * 注意：MethodArgumentNotValidException 只覆盖 @RequestBody，query 参数走的是 BindException
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        log.warn("参数校验失败: {}", message);
        return Result.fail(400, message);
    }

    /**
     * 兜底处理其他运行时异常
     * 只返回通用文案，不把内部信息（SQL、堆栈）暴露给客户端，原文仅写入日志
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("未预期的运行时异常", e);
        return Result.fail(400, "操作失败，请稍后重试");
    }

    /**
     * 处理其他未捕获的异常
     * 不回传异常类名与原始信息，仅返回错误码便于日志定位
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        String errorId = java.util.UUID.randomUUID().toString().substring(0, 8);
        log.error("系统内部错误[{}]", errorId, e);
        return Result.fail(500, "系统繁忙，请稍后重试（错误码：" + errorId + "）");
    }
}
