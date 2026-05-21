package com.example.library.repository;

import com.example.library.entity.Book;
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
public class BookRepository {

    private final JdbcTemplate jdbc;

    public BookRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Book> rowMapper = (rs, rowNum) -> {
        Book b = new Book();
        b.setId(rs.getLong("id"));
        b.setIsbn(rs.getString("isbn"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setPublisher(rs.getString("publisher"));
        b.setRetailPrice(rs.getBigDecimal("retail_price"));
        b.setStockQuantity(rs.getInt("stock_quantity"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        return b;
    };

    public Optional<Book> findById(Long id) {
        List<Book> list = jdbc.query("SELECT * FROM books WHERE id = ?", rowMapper, id);
        return list.stream().findFirst();
    }

    public Optional<Book> findByIsbn(String isbn) {
        List<Book> list = jdbc.query("SELECT * FROM books WHERE isbn = ?", rowMapper, isbn);
        return list.stream().findFirst();
    }

    public List<Book> search(String isbn, String title, String author, String publisher) {
        StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (isbn != null && !isbn.isBlank()) {
            sql.append(" AND isbn LIKE ?");
            params.add("%" + isbn + "%");
        }
        if (title != null && !title.isBlank()) {
            sql.append(" AND title LIKE ?");
            params.add("%" + title + "%");
        }
        if (author != null && !author.isBlank()) {
            sql.append(" AND author LIKE ?");
            params.add("%" + author + "%");
        }
        if (publisher != null && !publisher.isBlank()) {
            sql.append(" AND publisher LIKE ?");
            params.add("%" + publisher + "%");
        }
        sql.append(" ORDER BY id");
        return jdbc.query(sql.toString(), rowMapper, params.toArray());
    }

    public Long insert(Book book) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO books (isbn, title, author, publisher, retail_price, stock_quantity) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getPublisher());
            ps.setBigDecimal(5, book.getRetailPrice());
            ps.setInt(6, book.getStockQuantity() != null ? book.getStockQuantity() : 0);
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public int update(Book book) {
        return jdbc.update(
                "UPDATE books SET title=?, author=?, publisher=?, retail_price=?, stock_quantity=? WHERE id=?",
                book.getTitle(), book.getAuthor(), book.getPublisher(),
                book.getRetailPrice(), book.getStockQuantity(), book.getId());
    }

    public int updateStock(Long id, int quantity) {
        return jdbc.update("UPDATE books SET stock_quantity = stock_quantity + ? WHERE id=?", quantity, id);
    }

    public int deleteById(Long id) {
        return jdbc.update("DELETE FROM books WHERE id=?", id);
    }

    public int count() {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM books", Integer.class);
        return c != null ? c : 0;
    }
}
