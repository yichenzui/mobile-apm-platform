package com.monitor.entity.enums;

/**
 * 问题严重程度枚举
 */
public enum IssueSeverity {
    /**
     * 低
     */
    LOW(0, "低"),

    /**
     * 中
     */
    MEDIUM(1, "中"),

    /**
     * 高
     */
    HIGH(2, "高"),

    /**
     * 严重
     */
    CRITICAL(3, "严重");

    private final Integer code;
    private final String description;

    IssueSeverity(Integer code, String description) {
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
     * 根据代码获取严重程度
     * @param code 严重程度代码
     * @return 问题严重程度枚举
     */
    public static IssueSeverity fromCode(Integer code) {
        for (IssueSeverity severity : values()) {
            if (severity.getCode().equals(code)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("未知的严重程度代码: " + code);
    }
}
