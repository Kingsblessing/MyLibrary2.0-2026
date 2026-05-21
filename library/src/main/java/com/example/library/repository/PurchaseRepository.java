package com.example.library.repository;

import com.example.library.entity.Purchase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
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
        p.setBookId(rs.getObject("book_id") != null ? rs.getLong("book_id") : null);
        p.setIsbn(rs.getString("isbn"));
        p.setTitle(rs.getString("title"));
        p.setAuthor(rs.getString("author"));
        p.setPublisher(rs.getString("publisher"));
        p.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        p.setQuantity(rs.getInt("quantity"));
        p.setStatus(rs.getString("status"));
        p.setOperatorId(rs.getObject("operator_id") != null ? rs.getLong("operator_id") : null);
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) p.setCreatedAt(ct.toLocalDateTime());
        Timestamp ut = rs.getTimestamp("updated_at");
        if (ut != null) p.setUpdatedAt(ut.toLocalDateTime());
        try { p.setOperatorName(rs.getString("operator_name")); } catch (Exception ignored) {}
        return p;
    };

    public Optional<Purchase> findById(Long id) {
        String sql = "SELECT p.*, u.username AS operator_name FROM purchases p LEFT JOIN users u ON p.operator_id = u.id WHERE p.id = ?";
        List<Purchase> list = jdbc.query(sql, rowMapper, id);
        return list.stream().findFirst();
    }

    public List<Purchase> search(String status) {
        StringBuilder sql = new StringBuilder(
                "SELECT p.*, u.username AS operator_name FROM purchases p LEFT JOIN users u ON p.operator_id = u.id WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status = ?");
            params.add(status);
        }
        sql.append(" ORDER BY p.id DESC");
        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public void insert(Purchase purchase) {
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO purchases (book_id, isbn, title, author, publisher, purchase_price, quantity, status, operator_id) VALUES (?,?,?,?,?,?,?,?,?)");
            if (purchase.getBookId() != null) ps.setLong(1, purchase.getBookId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, purchase.getIsbn());
            ps.setString(3, purchase.getTitle());
            ps.setString(4, purchase.getAuthor());
            ps.setString(5, purchase.getPublisher());
            ps.setBigDecimal(6, purchase.getPurchasePrice());
            ps.setInt(7, purchase.getQuantity());
            ps.setString(8, purchase.getStatus());
            if (purchase.getOperatorId() != null) ps.setLong(9, purchase.getOperatorId());
            else ps.setNull(9, Types.INTEGER);
            return ps;
        });
    }

    public int updateStatus(Long id, String status) {
        return jdbc.update("UPDATE purchases SET status=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", status, id);
    }

    public int updateBookId(Long id, Long bookId) {
        return jdbc.update("UPDATE purchases SET book_id=?, updated_at=CURRENT_TIMESTAMP WHERE id=?", bookId, id);
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
