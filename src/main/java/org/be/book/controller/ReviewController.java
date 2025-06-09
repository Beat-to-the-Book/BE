package org.be.book.controller;

import lombok.RequiredArgsConstructor;
import org.be.auth.service.CustomUserDetails;
import org.be.book.dto.*;
import org.be.book.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> write(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.writeReview(userDetails.getUser(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long id,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                                 @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(userDetails.getUser(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        reviewService.deleteReview(userDetails.getUser(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<BookReviewSummaryResponse> getBookReviews(@PathVariable Long bookId) {
        return ResponseEntity.ok(reviewService.getReviewsByBook(bookId));
    }
}