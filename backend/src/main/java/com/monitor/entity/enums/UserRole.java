package com.monitor.entity.enums;

/**
 * 用户角色枚举
 */
public enum UserRole {
    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员"),

    /**
     * 普通用户
     */
    USER("USER", "普通用户");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
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
     * 根据代码获取角色
     * @param code 角色代码
     * @return 用户角色
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的角色代码: " + code);
    }
}
