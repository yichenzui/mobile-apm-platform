package com.monitor.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.monitor.entity.enums.UserRole;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Table("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户名
     */
    @Column("username")
    private String username;

    /**
     * 密码
     */
    @Column("password")
    private String password;

    /**
     * 真实姓名
     */
    @Column("real_name")
    private String realName;

    /**
     * 邮箱
     */
    @Column("email")
    private String email;

    /**
     * 手机号
     */
    @Column("phone")
    private String phone;

    /**
     * 头像URL
     */
    @Column("avatar")
    private String avatar;

    /**
     * 角色：ADMIN-管理员，USER-普通用户
     */
    @Column("role")
    private String role;

    /**
     * 获取角色枚举
     * @return 用户角色枚举
     */
    public UserRole getRoleEnum() {
        return UserRole.fromCode(role);
    }

    /**
     * 设置角色
     * @param roleEnum 角色枚举
     */
    public void setRoleEnum(UserRole roleEnum) {
        this.role = roleEnum.getCode();
    }

    /**
     * 状态：0-禁用，1-启用
     */
    @Column("status")
    private Integer status;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @Column("deleted")
    private Boolean deleted;
    
}
