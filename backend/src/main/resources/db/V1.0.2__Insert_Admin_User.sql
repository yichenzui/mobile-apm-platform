-- 插入管理员用户
-- 注意：password 仅在首次 INSERT 时写入；已存在时只同步其他字段，
-- 避免每次启动把用户自行修改过的密码覆盖掉。
-- 默认密码由 AdminPasswordInitializer 在检测到初始 hash 时统一设为 123456789。
INSERT INTO user (id, username, password, real_name, email, status, role) VALUES
(1, 'admin', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', '系统管理员', 'admin@example.com', 1, 'ADMIN')
ON DUPLICATE KEY UPDATE 
real_name = '系统管理员',
email = 'admin@example.com',
status = 1,
role = 'ADMIN';
