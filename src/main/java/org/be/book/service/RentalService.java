package org.be.book.service;

import org.be.point.dto.MilestoneAwardResponse;
import org.be.point.service.PointService;
import org.be.book.dto.RentalActiveResponse;
import org.be.book.dto.RentalResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.dto.AddRentalRequest;
import org.be.book.model.Book;
import org.be.book.model.Rental;
import org.be.book.repository.BookRepository;
import org.be.book.repository.RentalRepository;
import org.be.book.dto.ReturnRentalRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RentalService {
    private static final Logger log = LoggerFactory.getLogger(RentalService.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final RentalRepository rentalRepository;
    private final PointService pointService;

    public RentalService(UserRepository userRepository,
                         BookRepository bookRepository,
                         RentalRepository rentalRepository,
                         PointService pointService) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.rentalRepository = rentalRepository;
        this.pointService = pointService;
    }

    @Transactional(readOnly = true)
    public List<Book> getRentalHistory(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        List<Rental> rentals = rentalRepository.findByUser(user);

        return rentals.stream()
                .map(Rental::getBook)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RentalActiveResponse> getActiveRentals(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        List<Rental> activeRentals =
                rentalRepository.findByUserAndStatus(user, Rental.Status.RENTED);

        return activeRentals.stream()
                .map(rental -> {
                    LocalDate due = rental.getDueDate().toLocalDate();
                    long rawDays = ChronoUnit.DAYS.between(LocalDate.now(), due);
                    long daysRemaining = Math.max(0, rawDays);
                    return new RentalActiveResponse(
                            rental.getBook().getId(),
                            rental.getBook().getTitle(),
                            rental.getBook().getAuthor(),
                            rental.getBook().getPublisher(),
                            rental.getRentalDate().toLocalDate(),
                            due,
                            daysRemaining
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public RentalResponse addRental(AddRentalRequest addRentalRequest) {
        log.info("addRental 호출 - userId={}, bookId={}", addRentalRequest.getUserId(), addRentalRequest.getBookId());

        User user = userRepository.findByUserId(addRentalRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));
        log.debug("사용자 조회 성공: {}", user.getUserId());

        Book book = bookRepository.findById(addRentalRequest.getBookId())
                .orElseThrow(() -> new RuntimeException("대여할 책이 존재하지 않습니다."));
        log.debug("도서 조회 성공: {}", book.getTitle());

        boolean alreadyRenting =
                rentalRepository.existsByUserAndBookAndStatus(user, book, Rental.Status.RENTED);
        if (alreadyRenting) {
            log.warn("이미 해당 도서를 대여 중입니다. userId={}, bookId={}", user.getUserId(), book.getId());
            throw new IllegalStateException("이미 해당 도서를 대여 중입니다.");
        }

        log.debug("대여 전 재고: {}", book.getRentalStock());
        try {
            book.decreaseRentalStock();
        } catch (IllegalStateException e) {
            log.error("대여 실패 - 재고 부족: bookId={}, title={}", book.getId(), book.getTitle());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    e.getMessage()
            );
        }

        Rental rental = new Rental(user, book);
        Rental savedRental = rentalRepository.save(rental);

        MilestoneAwardResponse milestone = null;
        try {
            var result = pointService.checkAndAwardMilestone(user.getUserId());
            milestone = result; // 프론트 알림용으로 응답에 포함
            if (result.awarded() > 0) {
                log.info("🎯 마일스톤 보너스 지급: userId={}, milestone={}, bonus={}, totalPoints={}",
                        user.getUserId(), result.milestone(), result.awarded(), result.totalPoints());
            }
        } catch (Exception ex) {
            log.warn("⚠️ 마일스톤 포인트 지급 중 예외 발생(무시): {}", ex.getMessage());
        }

        log.info("대여 완료 - rentalId={}, userId={}, bookId={}", savedRental.getId(), user.getUserId(), book.getId());

        return new RentalResponse(
                savedRental.getId(),
                savedRental.getBook().getId(),
                savedRental.getBook().getTitle(),
                savedRental.getRentalDate(),
                savedRental.getDueDate(),
                savedRental.getReturnDate(),
                savedRental.getStatus().name(),
                milestone
        );
    }

    @Transactional
    public RentalResponse returnRental(ReturnRentalRequest req) {
        log.info("returnRental 호출 - userId={}, rentalId={}", req.getUserId(), req.getRentalId());

        User user = userRepository.findByUserId(req.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));
        log.debug("사용자 조회 성공: {}", user.getUserId());

        Rental rental = rentalRepository.findById(req.getRentalId())
                .orElseThrow(() -> new RuntimeException("대여 내역이 존재하지 않습니다."));
        log.debug("대여 내역 조회 성공: rentalId={}, bookId={}", rental.getId(), rental.getBook().getId());

        if (!rental.getUser().getId().equals(user.getId())) {
            log.warn("본인 대여 아님 - 요청자={}, 대여자={}", user.getUserId(), rental.getUser().getUserId());
            throw new RuntimeException("본인 대여가 아닙니다.");
        }

        log.debug("반납 전 재고: {}", rental.getBook().getRentalStock());
        rental.returnBook();
        rental.getBook().increaseRentalStock();

        log.info("반납 완료 - rentalId={}, userId={}, bookId={}", rental.getId(), user.getUserId(), rental.getBook().getId());

        return new RentalResponse(
                rental.getId(),
                rental.getBook().getId(),
                rental.getBook().getTitle(),
                rental.getRentalDate(),
                rental.getDueDate(),
                rental.getReturnDate(),
                rental.getStatus().name(),
                null
        );
    }
}