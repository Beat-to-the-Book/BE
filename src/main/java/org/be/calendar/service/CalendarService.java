package org.be.calendar.service;

import lombok.RequiredArgsConstructor;
import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.book.model.Book;
import org.be.book.repository.BookRepository;
import org.be.calendar.dto.CalendarCreateRequest;
import org.be.calendar.dto.CalendarMonthlyResponse;
import org.be.calendar.dto.CalendarResponse;
import org.be.calendar.model.Calendar;
import org.be.calendar.repository.CalendarRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CalendarRepository calendarRepository;

    @Transactional
    public CalendarResponse create(String userId, CalendarCreateRequest req) {
        User user = getUser(userId);
        Book book = getBook(req.getBookId());

        LocalDate start = parseDate(req.getStartDate(), "startDate");
        LocalDate end = parseDate(req.getEndDate(), "endDate");
        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate가 startDate보다 앞설 수 없습니다.");
        }

        Calendar saved = calendarRepository.save(
                new Calendar(user, book, start, end, req.getMemo())
        );
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CalendarResponse getOne(String userId, Long id) {
        User user = getUser(userId);
        Calendar entry = calendarRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정이 존재하지 않습니다."));
        return toResponse(entry);
    }

    @Transactional(readOnly = true)
    public List<CalendarResponse> getAll(String userId) {
        User user = getUser(userId);
        return calendarRepository.findByUserOrderByStartDateDesc(user)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CalendarMonthlyResponse getByMonth(String userId, int year, int month) {
        User user = getUser(userId);
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        List<CalendarResponse> items = calendarRepository.findMonthEntries(user, monthStart, monthEnd)
                .stream().map(this::toResponse).toList();
        return new CalendarMonthlyResponse(year, month, items);
    }

    // 선택: 수정/삭제 필요 시
    @Transactional
    public CalendarResponse update(String userId, Long id, CalendarCreateRequest req) {
        User user = getUser(userId);
        Calendar entry = calendarRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정이 존재하지 않습니다."));

        LocalDate start = parseDate(req.getStartDate(), "startDate");
        LocalDate end = parseDate(req.getEndDate(), "endDate");
        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate가 startDate보다 앞설 수 없습니다.");
        }
        Book book = getBook(req.getBookId());
        entry.update(start, end, req.getMemo(), book);
        return toResponse(entry);
    }

    @Transactional
    public void delete(String userId, Long id) {
        User user = getUser(userId);
        Calendar entry = calendarRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "일정이 존재하지 않습니다."));
        calendarRepository.delete(entry);
    }

    // helpers
    private User getUser(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자가 존재하지 않습니다."));
    }

    private Book getBook(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "도서가 존재하지 않습니다."));
    }

    private LocalDate parseDate(String s, String field) {
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " 형식이 올바르지 않습니다. (YYYY-MM-DD)");
        }
    }

    private CalendarResponse toResponse(Calendar c) {
        return new CalendarResponse(
                c.getId(),
                c.getBook().getId(),
                c.getBook().getTitle(),
                c.getBook().getAuthor(),
                c.getBook().getFrontCoverImageUrl(),
                c.getStartDate().toString(),
                c.getEndDate().toString(),
                c.getMemo()
        );
    }
}