package com.example.library.service;

import com.example.library.entity.Book;
import java.util.List;

public interface BookService {
    List<Book> listBooks(String isbn, String title, String author, String publisher);
    Book getBook(Long id);
    void addBook(Book book);
    void updateBook(Long id, Book book);
    void deleteBook(Long id);
    void stockIn(Long bookId, int quantity, Long purchaseId, Long operatorId);
}
