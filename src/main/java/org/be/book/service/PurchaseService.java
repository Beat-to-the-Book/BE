package org.be.book.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.dto.AddPurchaseRequest;
import org.be.book.model.Book;
import org.be.book.model.Purchase;
import org.be.book.repository.BookRepository;
import org.be.book.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

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

    public List<Purchase> getPurchaseHistory(Long userId) {
        return purchaseRepository.findByUserId(userId);
    }

    public Purchase addPurchase(AddPurchaseRequest addPurchaseRequest) {
        User user = userRepository.findByUserId(addPurchaseRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        Book book = bookRepository.findById(addPurchaseRequest.getBookId())
                .orElseThrow(() -> new RuntimeException("구매할 책이 존재하지 않습니다."));

        Purchase purchase = new Purchase(user, book);
        return purchaseRepository.save(purchase);
    }
}
