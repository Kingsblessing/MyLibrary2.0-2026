package com.example.library.service;

import com.example.library.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> listBooks(String isbn, String title, String author, String publisher);
    Book getBook(Long id);
    void addBook(Long purchaseId, java.math.BigDecimal retailPrice);
    void updateBook(Long id, Book book);
    void deleteBook(Long id);
    void stockIn(Long purchaseId, int quantity, Long operatorId, java.math.BigDecimal retailPrice);
    void sellBook(Long bookId, int quantity, Long operatorId);
}
