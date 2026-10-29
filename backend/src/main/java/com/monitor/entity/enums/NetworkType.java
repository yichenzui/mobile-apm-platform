package com.monitor.entity.enums;

/**
 * 网络类型枚举
 */
public enum NetworkType {
    /**
     * WiFi网络
     */
    WIFI("WIFI", "WiFi网络"),

    /**
     * 4G网络
     */
    FOUR_G("4G", "4G网络"),

    /**
     * 5G网络
     */
    FIVE_G("5G", "5G网络"),

    /**
     * 无网络
     */
    NONE("NONE", "无网络");

    private final String code;
    private final String description;

    NetworkType(String code, String description) {
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
     * 根据代码获取网络类型
     * @param code 网络类型代码
     * @return 网络类型枚举
     */
    public static NetworkType fromCode(String code) {
        for (NetworkType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的网络类型代码: " + code);
    }
}
