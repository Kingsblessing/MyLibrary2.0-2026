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
        // 1. 若关联了已有图书，自动填充图书信息
        if (purchase.getBookId() != null) {
            Book book = bookRepo.findById(purchase.getBookId())
                    .orElseThrow(() -> new BizException(ErrorCodes.BOOK_NOT_FOUND, "图书不存在"));
            purchase.setIsbn(book.getIsbn());
            purchase.setTitle(book.getTitle());
            purchase.setAuthor(book.getAuthor());
            purchase.setPublisher(book.getPublisher());
        }

        // 2. 必填校验
        if (purchase.getIsbn() == null || purchase.getIsbn().isBlank()) {
            throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN不能为空");
        }
        if (purchase.getTitle() == null || purchase.getTitle().isBlank()) {
            throw new BizException(ErrorCodes.BAD_REQUEST, "书名不能为空");
        }

        // 3. ISBN 校验位检查（支持带连字符的格式）
        String cleanIsbn = purchase.getIsbn().replaceAll("[^0-9]", "");  // 去除空格、连字符等
        if (!isValidIsbn13(cleanIsbn)) {
            throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN格式不正确或校验位错误");
        }
        purchase.setIsbn(cleanIsbn);  // 统一存储纯数字

        // 4. 如果未关联已有图书，检查 ISBN 是否已存在于 books 表
        if (purchase.getBookId() == null) {
            if (bookRepo.findByIsbn(cleanIsbn).isPresent()) {
                throw new BizException(ErrorCodes.ISBN_EXISTS, 
                        "该ISBN已存在对应的库存图书，请通过选择图书（提供bookId）创建进货单");
            }
        }

        purchase.setStatus("UNPAID");
        purchaseRepo.insert(purchase);
    }

    /**
     * 验证 ISBN‑13 校验位（只接受13位纯数字）
     */
    private boolean isValidIsbn13(String isbn) {
        if (isbn == null || isbn.length() != 13) {
            return false;
        }
        if (!isbn.matches("\\d{13}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.digit(isbn.charAt(i), 10);
            sum += (i % 2 == 0) ? digit : digit * 3; // 权重交替 1,3,1,3...
        }
        int check = (10 - (sum % 10)) % 10;
        int lastDigit = Character.digit(isbn.charAt(12), 10);
        return check == lastDigit;
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
