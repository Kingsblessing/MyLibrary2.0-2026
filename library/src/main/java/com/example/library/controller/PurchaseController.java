package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.entity.Purchase;
import com.example.library.service.PurchaseService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/list")
    public ApiResponse<List<Purchase>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(purchaseService.listPurchases(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<Purchase> detail(@PathVariable Long id) {
        return ApiResponse.success(purchaseService.getPurchase(id));
    }

    @PostMapping("/add")
    public ApiResponse<Void> add(@RequestBody Purchase purchase, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        purchase.setOperatorId(operatorId);
        purchaseService.addPurchase(purchase);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/pay")
    public ApiResponse<Void> pay(@PathVariable Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        purchaseService.payPurchase(id, operatorId);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/return")
    public ApiResponse<Void> returnPurchase(@PathVariable Long id) {
        purchaseService.returnPurchase(id);
        return ApiResponse.success();
    }
}
