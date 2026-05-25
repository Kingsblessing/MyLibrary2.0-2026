package com.example.library.service;

import com.example.library.entity.Book;
import java.util.List;

import java.util.Map;

public interface BookService {
    List<Book> listBooks(String isbn, String title, String author, String publisher);
    Map<String, Object> listBooks(String isbn, String title, String author, String publisher, int page, int size);
    Book getBook(Long id);
    void addBook(Long purchaseId, java.math.BigDecimal retailPrice);
    void updateBook(Long id, Book book);
    void deleteBook(Long id);
    void stockIn(Long purchaseId, int quantity, Long operatorId, java.math.BigDecimal retailPrice);
    void sellBook(Long bookId, int quantity, Long operatorId);
}
