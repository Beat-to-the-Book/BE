package org.be.book.service;

import lombok.RequiredArgsConstructor;
import org.be.auth.model.User;
import org.be.book.dto.*;
import org.be.book.model.Book;
import org.be.book.model.Review;
import org.be.book.repository.BookRepository;
import org.be.book.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    public ReviewResponse writeReview(User user, ReviewRequest request) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "책을 찾을 수 없습니다."));

        reviewRepository.findByBookAndAuthor(book, user)
                .ifPresent(r -> {
                    throw new ResponseStatusException(BAD_REQUEST, "이미 작성한 리뷰가 존재합니다.");
                });

        Review review = Review.builder()
                .author(user)
                .book(book)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse updateReview(User user, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> r.getAuthor().equals(user))
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "리뷰 수정 권한이 없습니다."));

        review.update(request.getRating(), request.getComment());
        return toResponse(review);
    }

    public void deleteReview(User user, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .filter(r -> r.getAuthor().equals(user))
                .orElseThrow(() -> new ResponseStatusException(FORBIDDEN, "리뷰 삭제 권한이 없습니다."));

        reviewRepository.delete(review);
    }

    public BookReviewSummaryResponse getReviewsByBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "책을 찾을 수 없습니다."));

        List<Review> reviews = reviewRepository.findAllByBook(book);
        List<ReviewResponse> responseList = reviews.stream().map(this::toResponse).toList();

        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        return BookReviewSummaryResponse.builder()
                .bookId(book.getId())
                .averageRating(Math.round(avg * 10.0) / 10.0)
                .reviews(responseList)
                .build();
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .author(review.getAuthor().getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .bookTitle(review.getBook().getTitle())
                .createdAt(review.getCreatedAt().toLocalDate())
                .build();
    }
}