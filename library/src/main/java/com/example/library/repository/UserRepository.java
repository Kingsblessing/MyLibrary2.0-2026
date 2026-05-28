package com.example.library.repository;

import com.example.library.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@SuppressWarnings("null")
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<User> rowMapper = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRealName(rs.getString("real_name"));
        u.setEmployeeId(rs.getString("employee_id"));
        u.setGender(rs.getString("gender"));
        u.setAge(rs.getObject("age") != null ? rs.getInt("age") : null);
        u.setRole(rs.getString("role"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());
        return u;
    };

    public Optional<User> findByUsername(String username) {
        List<User> list = jdbc.query("SELECT * FROM users WHERE username = ?", rowMapper, username);
        return list.stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        List<User> list = jdbc.query("SELECT * FROM users WHERE id = ?", rowMapper, id);
        return list.stream().findFirst();
    }

    public List<User> findAll() {
        return jdbc.query("SELECT * FROM users ORDER BY id", rowMapper);
    }

    public List<User> searchByUsername(String keyword) {
        return jdbc.query("SELECT * FROM users WHERE username LIKE ? ORDER BY id",
                rowMapper, "%" + keyword + "%");
    }

    public void insert(User user) {
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (username, password, real_name, employee_id, gender, age, role) VALUES (?,?,?,?,?,?,?)");
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getEmployeeId());
            ps.setString(5, user.getGender());
            if (user.getAge() != null) ps.setInt(6, user.getAge()); else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setString(7, user.getRole());
            return ps;
        });
    }

    public int update(User user) {
        return jdbc.update(
                "UPDATE users SET real_name=?, employee_id=?, gender=?, age=?, role=? WHERE id=?",
                user.getRealName(), user.getEmployeeId(), user.getGender(),
                user.getAge(), user.getRole(), user.getId());
    }

    public int updatePassword(Long id, String newPassword) {
        return jdbc.update("UPDATE users SET password=? WHERE id=?", newPassword, id);
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM users WHERE id=?", id);
    }

    public int count() {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        return c != null ? c : 0;
    }
}
