package org.be.book.controller;

import org.be.book.dto.AddRentalRequest;
import org.be.book.model.Rental;
import org.be.book.service.RentalService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<Rental>> getRentalHistory(@RequestParam String userId) {
        List<Rental> rentals = rentalService.getRentalHistory(userId);
        return ResponseEntity.ok(rentals);
    }

    // 관리자가 직접 대여 도서 추가
    @PostMapping("/add")
    public ResponseEntity<Rental> addRental(@RequestBody AddRentalRequest addRentalRequest) {
        return ResponseEntity.ok(rentalService.addRental(addRentalRequest));
    }
}
