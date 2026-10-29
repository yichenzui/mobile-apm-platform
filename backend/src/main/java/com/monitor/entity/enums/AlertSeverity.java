package com.monitor.entity.enums;

/**
 * 告警严重程度枚举
 */
public enum AlertSeverity {
    /**
     * 低
     */
    LOW("LOW", "低"),

    /**
     * 中
     */
    MEDIUM("MEDIUM", "中"),

    /**
     * 高
     */
    HIGH("HIGH", "高"),

    /**
     * 严重
     */
    CRITICAL("CRITICAL", "严重");

    private final String code;
    private final String description;

    AlertSeverity(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取严重程度
     * @param code 严重程度代码
     * @return 告警严重程度枚举
     */
    public static AlertSeverity fromCode(String code) {
        for (AlertSeverity severity : values()) {
            if (severity.getCode().equals(code)) {
                return severity;
            }
        }
        throw new IllegalArgumentException("未知的严重程度代码: " + code);
    }
}
