package com.example.library.repository;

import com.example.library.entity.Finance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class FinanceRepository {

    private final JdbcTemplate jdbc;

    public FinanceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Finance> rowMapper = (rs, rowNum) -> {
        Finance f = new Finance();
        f.setId(rs.getLong("id"));
        f.setType(rs.getString("type"));
        f.setAmount(rs.getBigDecimal("amount"));
        f.setDescription(rs.getString("description"));
        f.setRelatedId(rs.getObject("related_id") != null ? rs.getLong("related_id") : null);
        f.setOperatorId(rs.getLong("operator_id"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) f.setCreatedAt(ts.toLocalDateTime());
        return f;
    };

    private final RowMapper<Finance> detailMapper = (rs, rowNum) -> {
        Finance f = rowMapper.mapRow(rs, rowNum);
        f.setOperatorName(rs.getString("operator_name"));
        return f;
    };

    public Long insert(Finance finance) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO finances (type, amount, description, related_id, operator_id) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, finance.getType());
            ps.setBigDecimal(2, finance.getAmount());
            ps.setString(3, finance.getDescription());
            if (finance.getRelatedId() != null) ps.setLong(4, finance.getRelatedId()); else ps.setNull(4, java.sql.Types.BIGINT);
            ps.setLong(5, finance.getOperatorId());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<Finance> search(String type, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT f.*, u.username AS operator_name FROM finances f LEFT JOIN users u ON f.operator_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (type != null && !type.isBlank()) {
            sql.append(" AND f.type = ?");
            params.add(type);
        }
        if (startDate != null) {
            sql.append(" AND f.created_at >= ?");
            params.add(startDate.atStartOfDay());
        }
        if (endDate != null) {
            sql.append(" AND f.created_at < ?");
            params.add(endDate.plusDays(1).atStartOfDay());
        }
        sql.append(" ORDER BY f.id DESC");
        return jdbc.query(sql.toString(), detailMapper, params.toArray());
    }

    public BigDecimal sumByType(String type, LocalDate startDate, LocalDate endDate) {
        StringBuilder sql = new StringBuilder("SELECT COALESCE(SUM(amount), 0) FROM finances WHERE type = ?");
        List<Object> params = new ArrayList<>();
        params.add(type);
        if (startDate != null) {
            sql.append(" AND created_at >= ?");
            params.add(startDate.atStartOfDay());
        }
        if (endDate != null) {
            sql.append(" AND created_at < ?");
            params.add(endDate.plusDays(1).atStartOfDay());
        }
        BigDecimal result = jdbc.queryForObject(sql.toString(), BigDecimal.class, params.toArray());
        return result != null ? result : BigDecimal.ZERO;
    }

    public List<Map<String, Object>> monthlyReport(int year) {
        String sql = """
                SELECT
                    EXTRACT(MONTH FROM created_at)::int AS month,
                    COALESCE(SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END), 0) AS income,
                    COALESCE(SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END), 0) AS expense
                FROM finances
                WHERE EXTRACT(YEAR FROM created_at) = ?
                GROUP BY EXTRACT(MONTH FROM created_at)
                ORDER BY month
                """;
        return jdbc.queryForList(sql, year);
    }
}
