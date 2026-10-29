package com.monitor.entity.enums;

/**
 * 问题状态枚举
 */
public enum IssueStatus {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),

    /**
     * 处理中
     */
    IN_PROGRESS(1, "处理中"),

    /**
     * 已解决
     */
    RESOLVED(2, "已解决"),

    /**
     * 已忽略
     */
    IGNORED(3, "已忽略");

    private final Integer code;
    private final String description;

    IssueStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取状态
     * @param code 状态代码
     * @return 问题状态枚举
     */
    public static IssueStatus fromCode(Integer code) {
        for (IssueStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的状态代码: " + code);
    }
}
