package org.be.decoration.controller;

import lombok.RequiredArgsConstructor;
import org.be.decoration.dto.BuyDecorationRequest;
import org.be.decoration.dto.BuyDecorationResponse;
import org.be.decoration.dto.MyDecorationCountsResponse;
import org.be.decoration.service.DecorationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/decoration")
@RequiredArgsConstructor
public class DecorationController {

    private final DecorationService decorationService;

    @PostMapping("/buy")
    public ResponseEntity<BuyDecorationResponse> buy(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody BuyDecorationRequest req
    ) {
        return ResponseEntity.ok(decorationService.buy(user.getUsername(), req));
    }

    @GetMapping("/my-counts")
    public ResponseEntity<MyDecorationCountsResponse> myCounts(
            @AuthenticationPrincipal UserDetails user
    ) {
        return ResponseEntity.ok(decorationService.getMyCounts(user.getUsername()));
    }
}