package org.be.auth.controller;

import jakarta.validation.Valid;
import org.be.auth.dto.ApiResponse;
import org.be.auth.dto.LoginRequest;
import org.be.auth.dto.RegisterRequest;
import org.be.auth.dto.TokenResponse;
import org.be.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "회원가입 성공"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "로그인 성공", tokenResponse));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<TokenResponse>> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "토큰이 필요합니다."));
        }

        String token = authHeader.substring(7);
        boolean isValid = authService.validateToken(token);

        return isValid ?
                ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "유효한 토큰입니다.", new TokenResponse(token))) :
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다."));
    }
}
