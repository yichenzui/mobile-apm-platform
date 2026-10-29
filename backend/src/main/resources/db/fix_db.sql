USE mobile_apm;

-- =============================================
-- 1. 确保 user 表存在且结构正确
-- =============================================
CREATE TABLE IF NOT EXISTS `user`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    varchar(50)  NOT NULL COMMENT '用户名',
    `password`    varchar(100) NOT NULL COMMENT '密码',
    `real_name`   varchar(50)           DEFAULT NULL COMMENT '真实姓名',
    `email`       varchar(100)          DEFAULT NULL COMMENT '邮箱',
    `phone`       varchar(20)           DEFAULT NULL COMMENT '手机号',
    `avatar`      varchar(255)          DEFAULT NULL COMMENT '头像URL',
    `role`        varchar(20)  NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
    `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     tinyint      NOT NULL DEFAULT '0' COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 确保其他关键表存在（使用完整迁移版结构）
-- =============================================
CREATE TABLE IF NOT EXISTS `application`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `app_key`      varchar(50)  NOT NULL,
    `name`         varchar(100) NOT NULL,
    `package_name` varchar(100)          DEFAULT NULL,
    `platform`     varchar(20)           DEFAULT 'ANDROID',
    `description`  varchar(500)          DEFAULT NULL,
    `icon`         varchar(255)          DEFAULT NULL,
    `owner_id`     bigint                 DEFAULT NULL,
    `status`       tinyint      NOT NULL DEFAULT '1',
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      tinyint      NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_app_key` (`app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用表';

CREATE TABLE IF NOT EXISTS `user_application`
(
    `id`          bigint      NOT NULL AUTO_INCREMENT,
    `user_id`     bigint      NOT NULL,
    `app_key`     varchar(50) NOT NULL,
    `role`        varchar(20) NOT NULL DEFAULT 'VIEWER',
    `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_app` (`user_id`, `app_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `performance_summary`
(
    `id`                        bigint      NOT NULL AUTO_INCREMENT,
    `app_key`                   varchar(50) NOT NULL,
    `date`                      date        NOT NULL,
    `hour`                      tinyint              DEFAULT NULL,
    `active_users`              int                  DEFAULT '0',
    `new_users`                 int                  DEFAULT '0',
    `session_count`             int                  DEFAULT '0',
    `crash_count`               int                  DEFAULT '0',
    `lag_count`                 int                  DEFAULT '0',
    `network_request_count`     int                  DEFAULT '0',
    `network_failure_count`     int                  DEFAULT '0',
    `province`                  varchar(50)          DEFAULT NULL,
    `create_time`               datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`               datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY                         `idx_app_key` (`app_key`),
    KEY                         `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `performance_data`
(
    `id`           bigint       NOT NULL AUTO_INCREMENT,
    `app_key`      varchar(50)  NOT NULL,
    `device_id`    varchar(100) NOT NULL,
    `user_id`      varchar(100)          DEFAULT NULL,
    `session_id`   varchar(100)          DEFAULT NULL,
    `event_type`   varchar(50)  NOT NULL,
    `event_time`   datetime     NOT NULL,
    `event_data`   json                  DEFAULT NULL,
    `version_code` varchar(50)           DEFAULT NULL,
    `os_version`   varchar(50)           DEFAULT NULL,
    `network_type` varchar(20)           DEFAULT NULL,
    `create_time`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY            `idx_app_key` (`app_key`),
    KEY            `idx_device_id` (`device_id`),
    KEY            `idx_event_time` (`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `alert_record`
(
    `id`                bigint      NOT NULL AUTO_INCREMENT,
    `rule_id`           bigint      NOT NULL,
    `app_key`           varchar(50) NOT NULL,
    `metric`            varchar(50) NOT NULL,
    `value`             decimal(10, 2) NOT NULL,
    `threshold`         decimal(10, 2) NOT NULL,
    `severity`          varchar(20) NOT NULL DEFAULT 'MEDIUM',
    `status`            varchar(20) NOT NULL DEFAULT 'FIRING',
    `start_time`        datetime    NOT NULL,
    `end_time`          datetime              DEFAULT NULL,
    `message`           text,
    `notification_sent` tinyint      NOT NULL DEFAULT '0',
    `create_time`       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`       datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY                 `idx_app_key` (`app_key`),
    KEY                 `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `issue`
(
    `id`              bigint       NOT NULL AUTO_INCREMENT,
    `app_key`         varchar(50)  NOT NULL,
    `title`           varchar(200) NOT NULL,
    `description`     text,
    `type`            varchar(50)  NOT NULL DEFAULT 'CRASH',
    `severity`        varchar(20)  NOT NULL DEFAULT 'MEDIUM',
    `status`          varchar(20)  NOT NULL DEFAULT 'OPEN',
    `assignee_id`     bigint                DEFAULT NULL,
    `reporter_id`     bigint       NOT NULL,
    `stack_trace`     text,
    `reproduce_steps` text,
    `attachments`     json                  DEFAULT NULL,
    `create_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         tinyint      NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY               `idx_app_key` (`app_key`),
    KEY               `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 3. 插入/更新管理员用户 (密码: admin123)
-- BCrypt hash of "admin123"
-- =============================================
INSERT INTO `user` (`id`, `username`, `password`, `real_name`, `email`, `status`, `role`, `deleted`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrjPmfXtLa', '系统管理员', 'admin@example.com', 1, 'ADMIN', 0)
ON DUPLICATE KEY UPDATE 
password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P8jskrjPmfXtLa',
real_name = '系统管理员',
email = 'admin@example.com',
status = 1,
role = 'ADMIN',
deleted = 0;

-- =============================================
-- 4. 插入测试应用数据
-- =============================================
INSERT IGNORE INTO `application` (`id`, `app_key`, `name`, `package_name`, `platform`, `description`, `owner_id`, `status`, `deleted`) VALUES
(1, 'com.example.shopping', '购物APP', 'com.example.shopping', 'ANDROID', '一款主流电商购物应用', 1, 1, 0),
(2, 'com.example.video', '视频APP', 'com.example.video', 'ANDROID', '短视频社交平台', 1, 1, 0),
(3, 'com.example.news', '新闻资讯APP', 'com.example.news', 'ANDROID', '聚合多源新闻内容的阅读应用', 1, 1, 0);

-- =============================================
-- 5. 用户-应用关联
-- =============================================
INSERT IGNORE INTO `user_application` (`user_id`, `app_key`, `role`) VALUES
(1, 'com.example.shopping', 'OWNER'),
(1, 'com.example.video', 'OWNER'),
(1, 'com.example.news', 'OWNER');

-- =============================================
-- 6. 插入性能汇总测试数据（最近7天）
-- =============================================
INSERT IGNORE INTO `performance_summary` (`app_key`, `date`, `hour`, `active_users`, `session_count`, `crash_count`, `lag_count`, `network_request_count`, `network_failure_count`, `province`) VALUES
-- 今天
('com.example.shopping', CURDATE(), NULL, 12580, 28560, 15, 85, 158000, 2370, '广东'),
('com.example.shopping', CURDATE(), NULL, 3200, 7200, 5, 42, 42000, 840, '上海'),
('com.example.shopping', CURDATE(), NULL, 2800, 6500, 3, 28, 38000, 570, '北京'),
('com.example.video', CURDATE(), NULL, 8920, 22400, 28, 120, 265000, 3975, '广东'),
('com.example.video', CURDATE(), NULL, 2100, 5600, 12, 65, 68000, 1360, '北京'),
('com.example.news', CURDATE(), NULL, 5600, 14200, 8, 35, 95000, 950, '广东'),
('com.example.news', CURDATE(), NULL, 1800, 4500, 3, 18, 28000, 420, '浙江');

SELECT '=== 数据库初始化完成 ===' AS result;
SELECT id, username, role, status FROM user WHERE username = 'admin';
SELECT id, app_key, name FROM application;
