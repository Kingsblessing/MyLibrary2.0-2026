package com.example.library.service.impl;

import com.example.library.common.ErrorCodes;
import com.example.library.entity.Book;
import com.example.library.entity.Finance;
import com.example.library.entity.Purchase;
import com.example.library.exception.BizException;
import com.example.library.repository.BookRepository;
import com.example.library.repository.FinanceRepository;
import com.example.library.repository.PurchaseRepository;
import com.example.library.service.PurchaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepo;
    private final FinanceRepository financeRepo;
    private final BookRepository bookRepo;

    public PurchaseServiceImpl(PurchaseRepository purchaseRepo, FinanceRepository financeRepo, BookRepository bookRepo) {
        this.purchaseRepo = purchaseRepo;
        this.financeRepo = financeRepo;
        this.bookRepo = bookRepo;
    }

    @Override
    public List<Purchase> listPurchases(String status) {
        return purchaseRepo.search(status);
    }

    @Override
    public Purchase getPurchase(Long id) {
        return purchaseRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCodes.PURCHASE_NOT_FOUND, "进货记录不存在"));
    }

    @Override
    @Transactional
    public void addPurchase(Purchase purchase) {
        // If bookId provided (existing book in inventory), auto-fill book info
        if (purchase.getBookId() != null) {
            Book book = bookRepo.findById(purchase.getBookId())
                    .orElseThrow(() -> new BizException(ErrorCodes.BOOK_NOT_FOUND, "图书不存在"));
            purchase.setIsbn(book.getIsbn());
            purchase.setTitle(book.getTitle());
            purchase.setAuthor(book.getAuthor());
            purchase.setPublisher(book.getPublisher());
        }
        // Otherwise the request body must contain isbn, title, author, publisher
        if (purchase.getIsbn() == null || purchase.getIsbn().isBlank()) {
            throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN不能为空");
        }
        if (purchase.getTitle() == null || purchase.getTitle().isBlank()) {
            throw new BizException(ErrorCodes.BAD_REQUEST, "书名不能为空");
        }
        purchase.setStatus("UNPAID");
        purchaseRepo.insert(purchase);
    }

    @Override
    @Transactional
    public void payPurchase(Long id, Long operatorId) {
        Purchase purchase = purchaseRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCodes.PURCHASE_NOT_FOUND, "进货记录不存在"));
        if (!"UNPAID".equals(purchase.getStatus())) {
            throw new BizException(ErrorCodes.PURCHASE_STATUS_INVALID, "只有未付款的进货才能付款");
        }
        purchaseRepo.updateStatus(id, "PAID");

        // Record expense
        BigDecimal total = purchase.getPurchasePrice().multiply(BigDecimal.valueOf(purchase.getQuantity()));
        Finance finance = new Finance();
        finance.setType("EXPENSE");
        finance.setAmount(total);
        finance.setDescription("进货付款 - " + purchase.getTitle() + " x" + purchase.getQuantity());
        finance.setRelatedId(id);
        finance.setOperatorId(operatorId);
        financeRepo.insert(finance);
    }

    @Override
    public void returnPurchase(Long id) {
        Purchase purchase = purchaseRepo.findById(id)
                .orElseThrow(() -> new BizException(ErrorCodes.PURCHASE_NOT_FOUND, "进货记录不存在"));
        if (!"UNPAID".equals(purchase.getStatus())) {
            throw new BizException(ErrorCodes.PURCHASE_STATUS_INVALID, "只有未付款的进货才能退货");
        }
        purchaseRepo.updateStatus(id, "RETURNED");
    }
}
