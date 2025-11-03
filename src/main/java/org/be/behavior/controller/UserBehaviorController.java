package org.be.behavior.controller;

import lombok.RequiredArgsConstructor;
import org.be.auth.dto.ApiResponse;
import org.be.behavior.dto.UserBehaviorRequest;
import org.be.behavior.dto.UserBehaviorResponse;
import org.be.behavior.service.UserBehaviorService;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<UserBehaviorResponse>> logBehavior(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestBody UserBehaviorRequest request) {
        String userId = userDetails.getUsername();
        UserBehaviorResponse response = userBehaviorService.saveUserBehavior(userId, request);

        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "행동 로그 저장 완료", response));
    }
}
