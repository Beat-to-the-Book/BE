package org.be.book.service;

import org.be.book.model.Purchase;
import org.be.book.repository.PurchaseRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;

    public PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    public List<Purchase> getPurchaseHistory(Long userId) {
        return purchaseRepository.findByUserId(userId);
    }

    public Purchase addPurchase(Purchase purchase) {
        return purchaseRepository.save(purchase);
    }
}
