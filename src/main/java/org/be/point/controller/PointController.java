package org.be.point.controller;

import java.util.List;

import org.be.point.dto.*;
import org.be.point.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/me")
    public ResponseEntity<PointSummaryResponse> getMyPoints(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pointService.getMyPoints(user.getUsername()));
    }

    @GetMapping("/my-books")
    public ResponseEntity<List<UserBookItemResponse>> getMyBooks(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(pointService.getMyBooks(user.getUsername()));
    }

    @PostMapping("/throw")
    public ResponseEntity<ThrowBookResponse> throwBook(@AuthenticationPrincipal UserDetails user,
                                                       @RequestBody ThrowBookRequest req) {
        return ResponseEntity.ok(pointService.throwBook(user.getUsername(), req));
    }
}