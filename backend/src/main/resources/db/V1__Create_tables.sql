-- 创建应用表
CREATE TABLE IF NOT EXISTS application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_key VARCHAR(50) NOT NULL UNIQUE COMMENT '应用唯一标识',
    name VARCHAR(100) NOT NULL COMMENT '应用名称',
    package_name VARCHAR(100) COMMENT '应用包名',
    platform VARCHAR(20) COMMENT '平台：ANDROID，IOS',
    description VARCHAR(500) COMMENT '应用描述',
    icon VARCHAR(255) COMMENT '应用图标URL',
    owner_id BIGINT COMMENT '应用所有者ID',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_app_key (app_key),
    INDEX idx_owner_id (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用表';

-- 创建性能数据表
CREATE TABLE IF NOT EXISTS performance_data (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    device_id VARCHAR(100) NOT NULL COMMENT '设备ID',
    user_id VARCHAR(100) COMMENT '用户ID',
    session_id VARCHAR(100) COMMENT '会话ID',
    event_type VARCHAR(50) NOT NULL COMMENT '事件类型：startup/crash/anr/fps/memory/network',
    event_time TIMESTAMP NOT NULL COMMENT '事件发生时间',
    event_data JSON COMMENT '事件详情（JSON格式）',
    version_code VARCHAR(50) COMMENT '应用版本',
    os_version VARCHAR(50) COMMENT '系统版本',
    network_type VARCHAR(20) COMMENT '网络类型',
    province VARCHAR(50) COMMENT '省份（取自上报上下文 extras.province，用于地域聚合）',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_app_device (app_key, device_id),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='性能数据表';

-- 创建性能汇总表
CREATE TABLE IF NOT EXISTS performance_summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    date DATE NOT NULL COMMENT '日期',
    hour TINYINT COMMENT '小时，NULL表示按天汇总',
    active_users INT DEFAULT 0 COMMENT '活跃用户数',
    new_users INT DEFAULT 0 COMMENT '新增用户数',
    session_count INT DEFAULT 0 COMMENT '会话数',
    crash_count INT DEFAULT 0 COMMENT '崩溃数',
    lag_count INT DEFAULT 0 COMMENT '卡顿数',
    network_request_count INT DEFAULT 0 COMMENT '网络请求总数',
    network_failure_count INT DEFAULT 0 COMMENT '网络请求失败数',
    province VARCHAR(50) COMMENT '省份，为NULL表示不按省份汇总',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_app_date_hour (app_key, date, hour),
    INDEX idx_province (province)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='性能汇总表';

-- 创建告警规则表
CREATE TABLE IF NOT EXISTS alert_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_name VARCHAR(100) COMMENT '规则名称',
    rule_type VARCHAR(20) DEFAULT 'THRESHOLD' COMMENT '规则类型：THRESHOLD-阈值，TREND-趋势，YOY-同比，MOM-环比，ANOMALY-异常检测',
    metric VARCHAR(50) NOT NULL COMMENT '监控指标',
    -- condition 是 MySQL 保留字，必须加反引号，否则整段 CREATE TABLE 会语法失败
    `condition` VARCHAR(20) COMMENT '条件：GT-大于，GTE-大于等于，LT-小于，LTE-小于等于',
    condition_config JSON COMMENT '条件配置JSON',
    threshold DECIMAL(10,2) COMMENT '阈值',
    duration INT DEFAULT 60 COMMENT '持续时间（分钟）',
    severity VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '严重程度：LOW，MEDIUM，HIGH，CRITICAL',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    enabled TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    notification_channels JSON COMMENT '通知渠道：EMAIL，WEBHOOK，SMS',
    notification_config JSON COMMENT '通知配置JSON',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_app_key (app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警规则表';

-- 创建告警实例表
CREATE TABLE IF NOT EXISTS alert_instance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT COMMENT '规则ID',
    rule_name VARCHAR(100) COMMENT '规则名称',
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    metric VARCHAR(50) NOT NULL COMMENT '监控指标',
    severity VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '严重程度：LOW，MEDIUM，HIGH，CRITICAL',
    current_value DECIMAL(10,2) COMMENT '当前值',
    threshold_value VARCHAR(100) COMMENT '阈值',
    status VARCHAR(20) DEFAULT 'FIRING' COMMENT '状态：FIRING-告警中，RESOLVED-已恢复，ACKNOWLEDGED-已确认，CLOSED-已关闭',
    start_time TIMESTAMP NOT NULL COMMENT '开始时间',
    end_time TIMESTAMP COMMENT '结束时间',
    metric_time TIMESTAMP COMMENT '指标时间',
    message TEXT COMMENT '告警消息',
    assignee VARCHAR(50) COMMENT '处理人',
    process_time TIMESTAMP COMMENT '处理时间',
    process_note VARCHAR(500) COMMENT '处理备注',
    notify_channels JSON COMMENT '通知渠道配置JSON',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_rule_id (rule_id),
    INDEX idx_app_key (app_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警实例表';

-- 创建告警记录表
CREATE TABLE IF NOT EXISTS alert_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    rule_id BIGINT COMMENT '规则ID',
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    metric VARCHAR(50) NOT NULL COMMENT '监控指标',
    value DECIMAL(10,2) COMMENT '实际值',
    threshold DECIMAL(10,2) COMMENT '阈值',
    severity VARCHAR(20) DEFAULT 'MEDIUM' COMMENT '严重程度：LOW，MEDIUM，HIGH，CRITICAL',
    status VARCHAR(20) DEFAULT 'FIRING' COMMENT '状态：FIRING-告警中，RESOLVED-已恢复',
    start_time TIMESTAMP NOT NULL COMMENT '告警开始时间',
    end_time TIMESTAMP COMMENT '告警结束时间',
    message TEXT COMMENT '告警消息',
    notification_sent TINYINT DEFAULT 0 COMMENT '是否已发送通知：0-未发送，1-已发送',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_app_key (app_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='告警记录表';

-- 创建问题表
CREATE TABLE IF NOT EXISTS issue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    title VARCHAR(200) NOT NULL COMMENT '问题标题',
    description TEXT COMMENT '问题描述',
    type VARCHAR(50) NOT NULL COMMENT '问题类型：CRASH，ANR，PERFORMANCE，UI',
    severity TINYINT DEFAULT 1 COMMENT '严重程度：1-低，2-中，3-高，4-严重',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已解决，3-已忽略',
    assignee_id BIGINT COMMENT '指派人ID',
    reporter_id BIGINT COMMENT '报告人ID',
    stack_trace TEXT COMMENT '堆栈跟踪',
    reproduce_steps TEXT COMMENT '复现步骤',
    expected_result TEXT COMMENT '预期结果',
    actual_result TEXT COMMENT '实际结果',
    attachments JSON COMMENT '附件列表JSON',
    device_info JSON COMMENT '设备信息',
    occurrence_count INT DEFAULT 1 COMMENT '发生次数',
    affected_user_count INT DEFAULT 0 COMMENT '影响用户数',
    first_occurrence_time TIMESTAMP COMMENT '首次发生时间',
    last_occurrence_time TIMESTAMP COMMENT '最后发生时间',
    assignee VARCHAR(50) COMMENT '处理人',
    process_note VARCHAR(500) COMMENT '处理说明',
    process_time TIMESTAMP COMMENT '处理时间',
    resolved_time TIMESTAMP COMMENT '解决时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：false-未删除，true-已删除',
    INDEX idx_app_key (app_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建用户应用关联表
CREATE TABLE IF NOT EXISTS user_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    role VARCHAR(20) DEFAULT 'viewer' COMMENT '角色：admin-管理员，developer-开发者，viewer-查看者',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_app (user_id, app_key),
    INDEX idx_user_id (user_id),
    INDEX idx_app_key (app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户应用关联表';

-- 创建SDK配置表
CREATE TABLE IF NOT EXISTS sdk_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_key VARCHAR(50) NOT NULL COMMENT '应用标识',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_app_key (app_key, config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SDK配置表';

-- 插入示例数据
-- 注意：下面的 password 只是"初始占位 hash"（对应明文 123456，并非可用密码）。
-- 仅在首次 INSERT 时写入；已存在时只同步 role/status，绝不覆盖 password，
-- 否则每次启动都会把用户改过的密码重置掉。
-- 真实默认密码由 AdminPasswordInitializer 在检测到该占位 hash 时统一设置为 123456789。
INSERT INTO user (id, username, password, real_name, email, role, status) VALUES 
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 'admin@example.com', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE 
role = 'ADMIN',
status = 1;

INSERT INTO application (id, app_key, name, platform, description, owner_id, status) VALUES 
(1, 'test-app', '测试应用', 'ANDROID', '这是一个测试应用', 1, 1);

INSERT INTO sdk_config (app_key, config_key, config_value, description) VALUES 
('test-app', 'sample_rate', '100', '采样率，百分比'),
('test-app', 'network_enabled', 'true', '是否启用网络监控'),
('test-app', 'crash_enabled', 'true', '是否启用崩溃监控'),
('test-app', 'lag_enabled', 'true', '是否启用卡顿监控');
