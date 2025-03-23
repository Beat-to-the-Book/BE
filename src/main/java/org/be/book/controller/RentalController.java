package org.be.book.controller;

import org.be.book.model.Rental;
import org.be.book.service.RentalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rental")
public class RentalController {
    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Rental>> getRentalHistory(@PathVariable Long userId) {
        List<Rental> rentals = rentalService.getRentalHistory(userId);
        return ResponseEntity.ok(rentals);
    }

    @PostMapping
    public ResponseEntity<Rental> addRental(@RequestBody Rental rental) {
        return ResponseEntity.ok(rentalService.addRental(rental));
    }
}
