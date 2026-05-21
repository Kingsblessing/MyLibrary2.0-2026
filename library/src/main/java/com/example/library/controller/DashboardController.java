package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.repository.BookRepository;
import com.example.library.repository.FinanceRepository;
import com.example.library.repository.PurchaseRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final BookRepository bookRepo;
    private final PurchaseRepository purchaseRepo;
    private final FinanceRepository financeRepo;

    public DashboardController(BookRepository bookRepo, PurchaseRepository purchaseRepo, FinanceRepository financeRepo) {
        this.bookRepo = bookRepo;
        this.purchaseRepo = purchaseRepo;
        this.financeRepo = financeRepo;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalBooks", bookRepo.count());
        data.put("todayPurchases", purchaseRepo.countTodayCreated());
        data.put("pendingPurchases", purchaseRepo.countByStatus("UNPAID"));

        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        BigDecimal monthIncome = financeRepo.sumByType("INCOME", monthStart, now);
        data.put("monthIncome", monthIncome);

        return ApiResponse.success(data);
    }
}
