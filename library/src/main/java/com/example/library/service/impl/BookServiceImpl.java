package com.example.library.service.impl;

import com.example.library.common.ErrorCodes;
import com.example.library.entity.Book;
import com.example.library.entity.Finance;
import com.example.library.entity.Purchase;
import com.example.library.exception.BizException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.FinanceRepository;
import com.example.library.repository.PurchaseRepository;
import com.example.library.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepo;
    private final PurchaseRepository purchaseRepo;
    private final FinanceRepository financeRepo;

    public BookServiceImpl(BookRepository bookRepo, PurchaseRepository purchaseRepo, FinanceRepository financeRepo) {
        this.bookRepo = bookRepo;
        this.purchaseRepo = purchaseRepo;
        this.financeRepo = financeRepo;
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
    @Transactional
    public void addBook(Long purchaseId, BigDecimal retailPrice) {
        Purchase purchase = purchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new BizException(ErrorCodes.PURCHASE_NOT_FOUND, "进货记录不存在"));
        if (!"PAID".equals(purchase.getStatus())) {
            throw new BizException(ErrorCodes.PURCHASE_MUST_BE_PAID, "只有已付款的进货才能添加为图书");
        }
        if (retailPrice == null) {
            throw new BizException(ErrorCodes.BAD_REQUEST, "请填写零售价格");
        }
        if (bookRepo.findByIsbn(purchase.getIsbn()).isPresent()) {
            throw new BizException(ErrorCodes.ISBN_EXISTS, "ISBN已存在：" + purchase.getIsbn());
        }
        Book book = new Book();
        book.setIsbn(purchase.getIsbn());
        book.setTitle(purchase.getTitle());
        book.setAuthor(purchase.getAuthor());
        book.setPublisher(purchase.getPublisher());
        book.setRetailPrice(retailPrice);
        book.setStockQuantity(purchase.getQuantity());
        bookRepo.insert(book);
        purchaseRepo.updateStatus(purchaseId, "STOCKED");
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
    public void stockIn(Long purchaseId, int quantity, Long operatorId, BigDecimal retailPrice) {
        Purchase purchase = purchaseRepo.findById(purchaseId)
                .orElseThrow(() -> new BizException(ErrorCodes.PURCHASE_NOT_FOUND, "进货记录不存在"));
        if (!"PAID".equals(purchase.getStatus())) {
            throw new BizException(ErrorCodes.PURCHASE_MUST_BE_PAID, "已付款的进货才能入库");
        }

        Long bookId = purchase.getBookId();
        if (bookId != null) {
            // Existing book: update stock
            bookRepo.updateStock(bookId, quantity);
        } else {
            // New book: create book record with retail price
            if (retailPrice == null) {
                throw new BizException(ErrorCodes.BAD_REQUEST, "新书入库需要填写零售价格");
            }
            Book book = new Book();
            book.setIsbn(purchase.getIsbn());
            book.setTitle(purchase.getTitle());
            book.setAuthor(purchase.getAuthor());
            book.setPublisher(purchase.getPublisher());
            book.setRetailPrice(retailPrice);
            book.setStockQuantity(quantity);
            bookRepo.insert(book);
            bookId = bookRepo.findByIsbn(book.getIsbn()).get().getId();
            purchaseRepo.updateBookId(purchaseId, bookId);
        }
        purchaseRepo.updateStatus(purchaseId, "STOCKED");
    }

    @Override
    @Transactional
    public void sellBook(Long bookId, int quantity, Long operatorId) {
        Book book = bookRepo.findById(bookId)
                .orElseThrow(() -> new BizException(ErrorCodes.BOOK_NOT_FOUND, "图书不存在"));
        if (book.getStockQuantity() < quantity) {
            throw new BizException(ErrorCodes.STOCK_NOT_ENOUGH, "库存不足，当前库存：" + book.getStockQuantity());
        }
        if (book.getRetailPrice() == null) {
            throw new BizException(ErrorCodes.BAD_REQUEST, "该图书未设置零售价格，无法出售");
        }
        bookRepo.updateStock(bookId, -quantity);

        BigDecimal total = book.getRetailPrice().multiply(BigDecimal.valueOf(quantity));
        Finance finance = new Finance();
        finance.setType("INCOME");
        finance.setAmount(total);
        finance.setDescription("销售《" + book.getTitle() + "》x" + quantity);
        finance.setRelatedId(bookId);
        finance.setOperatorId(operatorId);
        financeRepo.insert(finance);
    }
}
