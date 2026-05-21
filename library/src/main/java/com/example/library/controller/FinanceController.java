package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.entity.Finance;
import com.example.library.service.FinanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/bills")
    public ApiResponse<Map<String, Object>> bills(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Finance> bills = financeService.listBills(type, startDate, endDate);
        BigDecimal totalIncome = financeService.getTotalIncome(startDate, endDate);
        BigDecimal totalExpense = financeService.getTotalExpense(startDate, endDate);

        Map<String, Object> data = new HashMap<>();
        data.put("bills", bills);
        data.put("totalIncome", totalIncome);
        data.put("totalExpense", totalExpense);
        data.put("netIncome", totalIncome.subtract(totalExpense));
        return ApiResponse.success(data);
    }

    @GetMapping("/report")
    public ApiResponse<Map<String, Object>> report(
            @RequestParam(required = false, defaultValue = "0") int year) {
        if (year <= 0) year = LocalDate.now().getYear();
        return ApiResponse.success(financeService.getReport(year));
    }
}
