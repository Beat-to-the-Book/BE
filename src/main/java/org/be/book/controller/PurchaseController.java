package org.be.book.controller;

import org.be.book.model.Purchase;
import org.be.book.service.PurchaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase")
public class PurchaseController {
    private final PurchaseService purchaseService;

    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Purchase>> getPurchaseHistory(@PathVariable Long userId) {
        List<Purchase> purchases = purchaseService.getPurchaseHistory(userId);
        return ResponseEntity.ok(purchases);
    }

    // 관리자가 직접 구매 도서 추가
    @PostMapping("/add")
    public ResponseEntity<Purchase> addPurchase(@RequestBody AddPurchaseRequest addPurchaseRequest) {
        return ResponseEntity.ok(purchaseService.addPurchase(addPurchaseRequest));
    }
}
