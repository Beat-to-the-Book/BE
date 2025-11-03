package org.be.book.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.dto.AddPurchaseRequest;
import org.be.book.model.Book;
import org.be.book.model.Purchase;
import org.be.book.repository.BookRepository;
import org.be.book.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.be.book.dto.PurchaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PurchaseRepository purchaseRepository;

    public PurchaseService(UserRepository userRepository,
                           BookRepository bookRepository,
                           PurchaseRepository purchaseRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchaseHistoryWithStatus(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));

        List<Purchase> purchases = purchaseRepository.findByUser(user);
        return purchases.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private PurchaseResponse toResponse(Purchase p) {
        return new PurchaseResponse(
                p.getId(),
                p.getBook().getId(),
                p.getBook().getTitle(),
                p.getStatus().name(),
                p.getPurchaseDate(),
                p.getRefundRequestedAt(),
                p.getRefundReason()
        );
    }

    public Purchase addPurchase(AddPurchaseRequest addPurchaseRequest) {
        User user = userRepository.findByUserId(addPurchaseRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        Book book = bookRepository.findById(addPurchaseRequest.getBookId())
                .orElseThrow(() -> new RuntimeException("구매할 책이 존재하지 않습니다."));

        Purchase purchase = new Purchase(user, book);
        return purchaseRepository.save(purchase);
    }

    @Transactional
    public PurchaseResponse requestRefund(org.be.book.dto.RefundPurchaseRequest req) {
        if (req == null || req.getPurchaseId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "purchaseId가 필요합니다.");
        }

        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));

        Purchase p = purchaseRepository.findById(req.getPurchaseId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "구매 내역이 존재하지 않습니다."));

        if (!p.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 구매 내역이 아닙니다.");
        }

        try {
            String reason = (req.getReason() == null || req.getReason().isBlank()) ? "사유 미입력" : req.getReason();
            p.requestRefund(reason);
            return toResponse(p);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
