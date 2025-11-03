package org.be.point.service;

import java.util.List;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.model.Book;
import org.be.book.repository.BookRepository;
import org.be.point.dto.*;
import org.be.point.model.UserBookRecord;
import org.be.point.repository.UserBookRecordRepository;
import org.be.point.repository.UserBookViewRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PointService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final UserBookRecordRepository userBookRecordRepository;
    private final UserBookViewRepository userBookViewRepository;

    public PointService(UserRepository userRepository,
                        BookRepository bookRepository,
                        UserBookRecordRepository userBookRecordRepository,
                        UserBookViewRepository userBookViewRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.userBookRecordRepository = userBookRecordRepository;
        this.userBookViewRepository = userBookViewRepository;
    }

    @Transactional(readOnly = true)
    public PointSummaryResponse getMyPoints(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));
        return new PointSummaryResponse(user.getTotalPoints());
    }

    @Transactional(readOnly = true)
    public List<UserBookItemResponse> getMyBooks(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));

        return userBookViewRepository.findUserBooksWithFlags(user.getId())
                .stream()
                .map(r -> new UserBookItemResponse(
                        r.getBookId(),
                        r.getTitle(),
                        r.getLeftCoverImageUrl(),
                        r.getFrontCoverImageUrl(),
                        r.getBackCoverImageUrl(),
                        r.getPurchased() != null && r.getPurchased() == 1,
                        r.getRented()    != null && r.getRented()    == 1,
                        r.getThrown()    != null && r.getThrown()    == 1
                ))
                .toList();
    }

    @Transactional
    public ThrowBookResponse throwBook(String userId, ThrowBookRequest req) {
        if (req == null || req.bookId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookId가 필요합니다.");
        }

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));

        Book book = bookRepository.findById(req.bookId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "도서가 존재하지 않습니다."));

        var rows = userBookViewRepository.findUserBooksWithFlags(user.getId());
        boolean owns = rows.stream().anyMatch(r -> r.getBookId().equals(book.getId()));
        if (!owns) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 사용자의 구매/대여 내역에 없는 도서입니다.");
        }

        UserBookRecord rec = userBookRecordRepository.findByUserAndBook(user, book)
                .orElseGet(() -> userBookRecordRepository.save(new UserBookRecord(user, book)));

        if (rec.isThrown()) {
            return new ThrowBookResponse(book.getId(), true, 0, user.getTotalPoints());
        }

        rec.markThrown();

        int awarded = 0;
        if (req.success()) {
            user.addPoints(5);
            awarded = 5;
        }

        return new ThrowBookResponse(book.getId(), true, awarded, user.getTotalPoints());
    }
}