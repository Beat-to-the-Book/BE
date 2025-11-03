package org.be.book.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.dto.CheckoutRequest;
import org.be.book.dto.CheckoutResponse;
import org.be.book.dto.PaymentWebhookRequest;
import org.be.book.model.Book;
import org.be.book.model.Order;
import org.be.book.model.Purchase;
import org.be.book.repository.BookRepository;
import org.be.book.repository.OrderRepository;
import org.be.book.repository.PurchaseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CheckoutService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final PurchaseRepository purchaseRepository;

    public CheckoutService(UserRepository userRepository,
                           BookRepository bookRepository,
                           OrderRepository orderRepository,
                           PurchaseRepository purchaseRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.orderRepository = orderRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public CheckoutResponse createOrder(String userId, CheckoutRequest req) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));
        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new RuntimeException("구매할 책이 존재하지 않습니다."));

        int qty = Math.max(1, req.getQuantity());
        long amount = Math.round(book.getPrice() * qty);

        Order order = orderRepository.save(new Order(user, book, qty, amount));
        return new CheckoutResponse(order.getId(), "/pay?orderId=" + order.getId());
    }

    @Transactional
    public void finalizeOrder(PaymentWebhookRequest body) {
        Order order = orderRepository.findById(body.getOrderId())
                .orElseThrow(() -> new RuntimeException("주문이 존재하지 않습니다."));

        if (order.getStatus() != Order.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 처리된 주문입니다.");
        }
        if (order.getExpiresAt() != null && order.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            order.markExpired();
            throw new ResponseStatusException(HttpStatus.GONE, "주문이 만료되었습니다.");
        }
        if (order.getAmount() != body.getPaidAmount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "금액 불일치");
        }
        // TODO: signature 검증(실 PG 연동 시)

        try {
            order.getBook().decreasePurchaseStock(order.getQuantity());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "구매 재고가 부족합니다.");
        }

        order.markPaid(body.getPgProvider(), body.getPgTransactionId());
        Purchase purchase = new Purchase(order.getUser(), order.getBook(), order.getQuantity());
        purchaseRepository.save(purchase);
    }

    // 개발용: PG 없이 성공 시뮬레이션
    @Transactional
    public void finalizeOrderForTest(String userId, Long orderId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("주문이 존재하지 않습니다."));

        PaymentWebhookRequest mock = new PaymentWebhookRequest();
        mock.setOrderId(orderId);
        mock.setPgProvider("TEST");
        mock.setPgTransactionId("TEST-" + orderId);
        mock.setPaidAmount(order.getAmount());
        mock.setSignature("TEST");
        finalizeOrder(mock);
    }
}