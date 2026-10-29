package com.monitor.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用实体类
 */
@Data
@Table("application")
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 应用唯一标识
     */
    @Column("app_key")
    private String appKey;

    /**
     * 应用名称
     */
    @Column("name")
    private String name;

    /**
     * 应用包名
     */
    @Column("package_name")
    private String packageName;

    /**
     * 平台：ANDROID，IOS
     */
    @Column("platform")
    private String platform;

    /**
     * 应用描述
     */
    @Column("description")
    private String description;

    /**
     * 应用图标URL
     */
    @Column("icon")
    private String icon;

    /**
     * 应用所有者ID
     */
    @Column("owner_id")
    private Long ownerId;

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
