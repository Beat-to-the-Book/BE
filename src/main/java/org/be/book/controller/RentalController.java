package org.be.book.controller;

import org.be.book.dto.AddRentalRequest;
import org.be.book.dto.RentalActiveResponse;
import org.be.book.dto.RentalResponse;
import org.be.book.model.Book;
import org.be.book.model.Rental;
import org.be.book.dto.ReturnRentalRequest;
import org.be.book.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rental")
public class RentalController {
    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    // 특정 유저의 대여 기록 조회
    @GetMapping("/history")
    public ResponseEntity<List<Book>> getRentalHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();

        List<Book> rentedBooks = rentalService.getRentalHistory(userId);
        return ResponseEntity.ok(rentedBooks);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RentalActiveResponse>> getActiveRentals(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        return ResponseEntity.ok(rentalService.getActiveRentals(userId));
    }

    // 사용자가 대여 버튼을 눌렀을 때 대여 생성
    @PostMapping
    public ResponseEntity<RentalResponse> addRental(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddRentalRequest addRentalRequest) {
        addRentalRequest.setUserId(userDetails.getUsername());
        return ResponseEntity.ok(rentalService.addRental(addRentalRequest));
    }

    // 반납 엔드포인트 추가
    @PostMapping("/return")
    public ResponseEntity<RentalResponse> returnRental(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody ReturnRentalRequest req) {
        req.setUserId(user.getUsername());
        RentalResponse response = rentalService.returnRental(req);
        return ResponseEntity.ok(response);
    }
}
