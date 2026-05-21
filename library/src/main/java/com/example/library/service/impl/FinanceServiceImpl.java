package com.example.library.service.impl;

import com.example.library.entity.Finance;
import com.example.library.repository.FinanceRepository;
import com.example.library.service.FinanceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class FinanceServiceImpl implements FinanceService {

    private final FinanceRepository financeRepo;

    public FinanceServiceImpl(FinanceRepository financeRepo) {
        this.financeRepo = financeRepo;
    }

    @Override
    public List<Finance> listBills(String type, LocalDate startDate, LocalDate endDate) {
        return financeRepo.search(type, startDate, endDate);
    }

    @Override
    public Map<String, Object> getReport(int year) {
        List<Map<String, Object>> monthly = financeRepo.monthlyReport(year);
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;

        List<Map<String, Object>> monthDetails = new ArrayList<>();
        for (Map<String, Object> row : monthly) {
            int month = (int) row.get("month");
            BigDecimal income = new BigDecimal(row.get("income").toString());
            BigDecimal expense = new BigDecimal(row.get("expense").toString());
            BigDecimal net = income.subtract(expense);
            totalIncome = totalIncome.add(income);
            totalExpense = totalExpense.add(expense);

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("month", month);
            detail.put("income", income);
            detail.put("expense", expense);
            detail.put("netIncome", net);
            if (income.compareTo(BigDecimal.ZERO) > 0) {
                detail.put("profitRate", net.multiply(BigDecimal.valueOf(100)).divide(income, 2, java.math.RoundingMode.HALF_UP));
            } else {
                detail.put("profitRate", BigDecimal.ZERO);
            }
            monthDetails.add(detail);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("year", year);
        result.put("monthly", monthDetails);
        result.put("totalIncome", totalIncome);
        result.put("totalExpense", totalExpense);
        result.put("netIncome", totalIncome.subtract(totalExpense));
        return result;
    }

    @Override
    public BigDecimal getTotalIncome(LocalDate startDate, LocalDate endDate) {
        return financeRepo.sumByType("INCOME", startDate, endDate);
    }

    @Override
    public BigDecimal getTotalExpense(LocalDate startDate, LocalDate endDate) {
        return financeRepo.sumByType("EXPENSE", startDate, endDate);
    }
}
