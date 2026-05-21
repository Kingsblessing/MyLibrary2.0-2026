package com.example.library.service;

import com.example.library.entity.Purchase;
import java.util.List;

public interface PurchaseService {
    List<Purchase> listPurchases(String status);
    Purchase getPurchase(Long id);
    void addPurchase(Purchase purchase);
    void payPurchase(Long id, Long operatorId);
    void returnPurchase(Long id);
}
