package com.example.library.service.impl;

import com.example.library.common.ErrorCodes;
import com.example.library.entity.Finance;
import com.example.library.entity.Purchase;
import com.example.library.exception.BizException;
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

    public PurchaseServiceImpl(PurchaseRepository purchaseRepo, FinanceRepository financeRepo) {
        this.purchaseRepo = purchaseRepo;
        this.financeRepo = financeRepo;
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
    public void addPurchase(Purchase purchase) {
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
        finance.setDescription("进货付款 - 进货单#" + id);
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
