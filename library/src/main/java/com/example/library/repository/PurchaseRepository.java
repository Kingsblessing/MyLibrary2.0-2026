package com.example.library.repository;

import com.example.library.entity.Purchase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PurchaseRepository {

    private final JdbcTemplate jdbc;

    public PurchaseRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Purchase> rowMapper = (rs, rowNum) -> {
        Purchase p = new Purchase();
        p.setId(rs.getLong("id"));
        p.setBookId(rs.getLong("book_id"));
        p.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setStatus(rs.getString("status"));
        p.setOperatorId(rs.getLong("operator_id"));
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) p.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) p.setUpdatedAt(ut.toLocalDateTime());
        return p;
    };

    private final RowMapper<Purchase> detailMapper = (rs, rowNum) -> {
        Purchase p = rowMapper.mapRow(rs, rowNum);
        p.setBookTitle(rs.getString("book_title"));
        p.setBookIsbn(rs.getString("book_isbn"));
        p.setOperatorName(rs.getString("operator_name"));
        return p;
    };

    public Optional<Purchase> findById(Long id) {
        String sql = "SELECT p.*, b.title AS book_title, b.isbn AS book_isbn, u.username AS operator_name " +
                "FROM purchases p LEFT JOIN books b ON p.book_id = b.id LEFT JOIN users u ON p.operator_id = u.id " +
                "WHERE p.id = ?";
        List<Purchase> list = jdbc.query(sql, detailMapper, id);
        return list.stream().findFirst();
    }

    public List<Purchase> search(String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, b.title AS book_title, b.isbn AS book_isbn, u.username AS operator_name " +
                        "FROM purchases p LEFT JOIN books b ON p.book_id = b.id LEFT JOIN users u ON p.operator_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY p.id DESC");
        return jdbc.query(sql.toString(), detailMapper, params.toArray());
    }

    public Long insert(Purchase purchase) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO purchases (book_id, purchase_price, quantity, status, operator_id) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, purchase.getBookId());
            ps.setBigDecimal(2, purchase.getPurchasePrice());
            ps.setInt(3, purchase.getQuantity());
            ps.setString(4, purchase.getStatus());
            ps.setLong(5, purchase.getOperatorId());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int updateStatus(Long id, String status) {
        return jdbc.update("UPDATE purchases SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", status, id);
    }

    public int countByStatus(String status) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM purchases WHERE status=?", Integer.class, status);
        return c != null ? c : 0;
    }

    public int countTodayCreated() {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM purchases WHERE created_at::date = CURRENT_DATE", Integer.class);
        return c != null ? c : 0;
    }
}
