-- Super admin: username=admin, password=admin123 (MD5 encrypted)
INSERT INTO users (username, password, real_name, employee_id, gender, age, role)
VALUES ('admin', '0192023a7bbd73250516f069df18b500', '系统管理员', 'EMP001', '男', 30, 'SUPER')
ON CONFLICT (username) DO NOTHING;
