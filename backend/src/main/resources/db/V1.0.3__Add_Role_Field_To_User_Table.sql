-- 为用户表添加role字段
ALTER TABLE user ADD COLUMN role VARCHAR(20) DEFAULT 'USER' COMMENT '角色：ADMIN-管理员，USER-普通用户';

-- 更新现有管理员用户的角色
UPDATE user SET role = 'ADMIN' WHERE username = 'admin';
