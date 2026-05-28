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

        // 3. 【统一格式化】清洗、校验、转换为 13 位纯数字
        String normalizedIsbn = normalizeIsbn(purchase.getIsbn());
        purchase.setIsbn(normalizedIsbn);

        // 4. 查重：仅当未关联已有图书时才检查（同一本书允许重复进货）
        if (purchase.getBookId() == null) {
            if (bookRepo.findByIsbn(normalizedIsbn).isPresent()) {
                throw new BizException(ErrorCodes.ISBN_EXISTS,
                        "该ISBN已存在对应的库存图书，请通过选择图书（提供bookId）创建进货单");
            }
        }

        purchase.setStatus("UNPAID");
        purchaseRepo.insert(purchase);
    }

    /**
     * 将输入的 ISBN 统一清洗并转换为 13 位纯数字格式
     * @param rawIsbn 用户输入的原始字符串（可能含连字符、空格等）
     * @return 13 位纯数字 ISBN
     * @throws BizException 如果格式不正确或校验位错误
     */
    private String normalizeIsbn(String rawIsbn) {
        // 去除所有非数字和 X 的字符
        String cleaned = rawIsbn.replaceAll("[^0-9Xx]", "").toUpperCase();

        if (cleaned.length() == 13) {
            if (!isValidIsbn13(cleaned)) {
                throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN（13位）校验位错误");
            }
            return cleaned;
        } else if (cleaned.length() == 10) {
            if (!isValidIsbn10(cleaned)) {
                throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN（10位）校验位错误");
            }
            return convert10to13(cleaned);
        } else {
            throw new BizException(ErrorCodes.BAD_REQUEST, "ISBN长度必须为10位或13位");
        }
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
