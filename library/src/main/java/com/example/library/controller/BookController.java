package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.entity.Book;
import com.example.library.service.BookService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Book>> list(
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher) {
        return ApiResponse.success(bookService.listBooks(isbn, title, author, publisher));
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> detail(@PathVariable Long id) {
        return ApiResponse.success(bookService.getBook(id));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody Book book) {
        bookService.addBook(book);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody Book book) {
        bookService.updateBook(id, book);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success();
    }

    @PostMapping("/stock-in")
    public ApiResponse<Void> stockIn(@RequestBody Map<String, Object> body) {
        Long bookId = Long.valueOf(body.get("bookId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        Long purchaseId = body.get("purchaseId") != null ? Long.valueOf(body.get("purchaseId").toString()) : null;
        Long operatorId = Long.valueOf(body.get("operatorId").toString());
        bookService.stockIn(bookId, quantity, purchaseId, operatorId);
        return ApiResponse.success();
    }
}
