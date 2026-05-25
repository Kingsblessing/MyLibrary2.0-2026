package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.entity.Book;
import com.example.library.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
    public ApiResponse<?> list(
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String publisher,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ApiResponse.success(bookService.listBooks(isbn, title, author, publisher, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Book> detail(@PathVariable Long id) {
        return ApiResponse.success(bookService.getBook(id));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody Map<String, Object> body) {
        Long purchaseId = Long.valueOf(body.get("purchaseId").toString());
        BigDecimal retailPrice = new BigDecimal(body.get("retailPrice").toString());
        bookService.addBook(purchaseId, retailPrice);
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
    public ApiResponse<Void> stockIn(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long purchaseId = Long.valueOf(body.get("purchaseId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        Long operatorId = (Long) request.getAttribute("userId");
        BigDecimal retailPrice = body.get("retailPrice") != null
                ? new BigDecimal(body.get("retailPrice").toString()) : null;
        bookService.stockIn(purchaseId, quantity, operatorId, retailPrice);
        return ApiResponse.success();
    }

    @PostMapping("/sell")
    public ApiResponse<Void> sell(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long bookId = Long.valueOf(body.get("bookId").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());
        Long operatorId = (Long) request.getAttribute("userId");
        bookService.sellBook(bookId, quantity, operatorId);
        return ApiResponse.success();
    }
}
