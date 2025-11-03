package org.be.book.controller;

import org.be.book.dto.CheckoutRequest;
import org.be.book.dto.CheckoutResponse;
import org.be.book.dto.PaymentWebhookRequest;
import org.be.book.service.CheckoutService;
import org.be.point.dto.MilestoneAwardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase")
public class CheckoutController {
    private final CheckoutService checkoutService;
    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@AuthenticationPrincipal UserDetails user,
                                                     @RequestBody CheckoutRequest req) {
        return ResponseEntity.ok(checkoutService.createOrder(user.getUsername(), req));
    }

    // PG 웹훅(실서버에서 PG가 호출)
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody PaymentWebhookRequest body) {
        checkoutService.finalizeOrder(body);
        return ResponseEntity.ok().build();
    }

    // 개발용: PG 없이 결제 성공 시뮬레이션
    @PostMapping("/confirm")
    public ResponseEntity<CheckoutResponse> confirmForDev(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam Long orderId) {
        CheckoutResponse response = checkoutService.finalizeOrderForTest(user.getUsername(), orderId);
        return ResponseEntity.ok(response);
    }
}