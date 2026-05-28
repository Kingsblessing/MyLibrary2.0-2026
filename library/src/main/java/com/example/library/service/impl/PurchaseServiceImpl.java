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
        // 1. 若关联了已有图书，自动填充图书信息（此时 ISBN 已由系统保证正确，无需再校验）
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

        // 3. 清除分隔符，保留数字及可能的 X（仅 10 位 ISBN 允许 X）
        String rawIsbn = purchase.getIsbn().replaceAll("[^0-9Xx]", "").toUpperCase();

        // 4. 根据长度选择校验方式，并统一转为 13 位纯数字存储
        String normalizedIsbn;
        if (rawIsbn.length() == 13) {
            if (!isValidIsbn13(rawIsbn)) {
                throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN（13位）校验位错误");
            }
            normalizedIsbn = rawIsbn;
        } else if (rawIsbn.length() == 10) {
            if (!isValidIsbn10(rawIsbn)) {
                throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN（10位）校验位错误");
            }
            normalizedIsbn = convert10to13(rawIsbn);
        } else {
            throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN长度必须为10位或13位");
        }

        purchase.setIsbn(normalizedIsbn);   // 统一存储 13 位纯数字

        // 5. 查重：仅当未关联已有图书时才检查（同一本书允许重复进货）
        if (purchase.getBookId() == null) {
            if (bookRepo.findByIsbn(normalizedIsbn).isPresent()) {
                throw new BizException(ErrorCodes.ISBN_EXISTS,
                        "该ISBN已存在对应的库存图书，请通过选择图书（提供bookId）创建进货单");
            }
        }

        purchase.setStatus("UNPAID");
        purchaseRepo.insert(purchase);
    }

    // ---------- 辅助校验方法 ----------

    /**
     * 验证 ISBN‑13 校验位（纯数字）
     */
    private boolean isValidIsbn13(String isbn) {
        if (isbn == null || isbn.length() != 13 || !isbn.matches("\\d{13}")) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.digit(isbn.charAt(i), 10);
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        int lastDigit = Character.digit(isbn.charAt(12), 10);
        return check == lastDigit;
    }

    /**
     * 验证 ISBN‑10 校验位（允许末位为 X 表示 10）
     */
    private boolean isValidIsbn10(String isbn) {
        if (isbn == null || isbn.length() != 10) {
            return false;
        }
        // 前9位必须是数字，第10位可以是数字或'X'
        if (!isbn.substring(0, 9).matches("\\d{9}")) {
            return false;
        }
        char last = isbn.charAt(9);
        if (!Character.isDigit(last) && last != 'X') {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.digit(isbn.charAt(i), 10) * (10 - i);
        }
        sum += (last == 'X') ? 10 : Character.digit(last, 10);
        return sum % 11 == 0;
    }

    /**
     * 将有效的 10 位 ISBN 转换为 13 位
     * 规则：取前 9 位，在前面加上 "978"，重新计算校验位
     */
    private String convert10to13(String isbn10) {
        String prefix = "978" + isbn10.substring(0, 9);
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.digit(prefix.charAt(i), 10);
            sum += (i % 2 == 0) ? digit : digit * 3;
        }
        int check = (10 - (sum % 10)) % 10;
        return prefix + check;
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
