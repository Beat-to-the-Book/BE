package org.be.book.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.dto.AddRentalRequest;
import org.be.book.model.Book;
import org.be.book.model.Rental;
import org.be.book.repository.BookRepository;
import org.be.book.repository.RentalRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RentalService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final RentalRepository rentalRepository;

    public RentalService(UserRepository userRepository,
                         BookRepository bookRepository,
                         RentalRepository rentalRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<Rental> getRentalHistory(Long userId) {
        return rentalRepository.findByUserId(userId);
    }

    public Rental addRental(AddRentalRequest addRentalRequest) {
        User user = userRepository.findByUserId(addRentalRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        Book book = bookRepository.findById(addRentalRequest.getBookId())
                .orElseThrow(() -> new RuntimeException("대여할 책이 존재하지 않습니다."));

        Rental rental = new Rental(user, book);

        return rentalRepository.save(rental);
    }
}
