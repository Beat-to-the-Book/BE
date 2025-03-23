package org.be.book.service;

import org.be.book.model.Rental;
import org.be.book.repository.RentalRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RentalService {
    private final RentalRepository rentalRepository;

    public RentalService(RentalRepository rentalRepository) {
        this.rentalRepository = rentalRepository;
    }

    public List<Rental> getRentalHistory(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public Rental addRental(Rental rental) {
        return rentalRepository.save(rental);
    }
}
