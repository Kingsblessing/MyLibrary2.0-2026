package com.example.library.service.impl;

import com.example.library.common.ErrorCodes;
import com.example.library.entity.Book;
import com.example.library.exception.BizException;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepo;

    public BookServiceImpl(BookRepository bookRepo) {
        this.bookRepo = bookRepo;
    }

    @Override
    public List<Book> listBooks(String isbn, String title, String author, String publisher) {
        return bookRepo.search(isbn, title, author, publisher);
    }

    @Override
    public Book getBook(Long id) {
        return bookRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCodes.BOOK_NOT_FOUND, "图书不存在"));
    }

    @Override
    public void addBook(Book book) {
        if (bookRepo.findByIsbn(book.getIsbn()).isPresent()) {
            throw new BizException(ErrorCodes.ISBN_EXISTS, "ISBN已存在");
        }
        if (book.getStockQuantity() == null) book.setStockQuantity(0);
        bookRepo.insert(book);
    }

    @Override
    public void updateBook(Long id, Book book) {
        Book existing = bookRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCodes.BOOK_NOT_FOUND, "图书不存在"));
        existing.setTitle(book.getTitle());
        existing.setAuthor(book.getAuthor());
        existing.setPublisher(book.getPublisher());
        existing.setRetailPrice(book.getRetailPrice());
        if (book.getStockQuantity() != null) existing.setStockQuantity(book.getStockQuantity());
        bookRepo.update(existing);
    }

    @Override
    public void deleteBook(Long id) {
        if (!bookRepo.findById(id).isPresent()) {
            throw new BizException(ErrorCodes.BOOK_NOT_FOUND, "图书不存在");
        }
        bookRepo.deleteById(id);
    }

    @Override
    @Transactional
    public void stockIn(Long bookId, int quantity, Long purchaseId, Long operatorId) {
        bookRepo.updateStock(bookId, quantity);
    }
}
