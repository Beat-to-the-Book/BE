package org.be.behavior.controller;

import lombok.RequiredArgsConstructor;
import org.be.behavior.dto.UserBehaviorRequest;
import org.be.behavior.service.UserBehaviorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/behavior")
@RequiredArgsConstructor
public class UserBehaviorController {

    private final UserBehaviorService userBehaviorService;

    @PostMapping("/log")
    public ResponseEntity<Void> logBehavior(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody UserBehaviorRequest request) {
        String userId = userDetails.getUsername();

        userBehaviorService.saveUserBehavior(userId, request);
        return ResponseEntity.ok().build();
    }
}
