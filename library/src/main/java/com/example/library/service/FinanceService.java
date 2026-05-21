package com.example.library.service;

import com.example.library.entity.Finance;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FinanceService {
    List<Finance> listBills(String type, LocalDate startDate, LocalDate endDate);
    Map<String, Object> getReport(int year);
    BigDecimal getTotalIncome(LocalDate startDate, LocalDate endDate);
    BigDecimal getTotalExpense(LocalDate startDate, LocalDate endDate);
}
