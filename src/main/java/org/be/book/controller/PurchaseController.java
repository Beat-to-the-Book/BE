package org.be.book.controller;

import org.be.book.dto.AddPurchaseRequest;
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

    // 특정 유저의 대여 기록 조회
    @GetMapping("/history")
    public ResponseEntity<List<Purchase>> getPurchaseHistory(@RequestParam String userId) {
        List<Purchase> purchases = purchaseService.getPurchaseHistory(userId);
        return ResponseEntity.ok(purchases);
    }

    // 관리자가 직접 구매 도서 추가
    @PostMapping("/add")
    public ResponseEntity<Purchase> addPurchase(@RequestBody AddPurchaseRequest addPurchaseRequest) {
        return ResponseEntity.ok(purchaseService.addPurchase(addPurchaseRequest));
    }
}
