-- Super admin: username=admin, password=admin123 (MD5 encrypted)
INSERT INTO users (username, password, real_name, employee_id, gender, age, role)
VALUES ('admin', 'c3284d0f94606de1fd2af172aba15bf3', '系统管理员', 'EMP001', '男', 30, 'SUPER')
ON CONFLICT (username) DO NOTHING;
