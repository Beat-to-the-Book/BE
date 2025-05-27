package org.be.auth.service;

import org.be.auth.config.JwtTokenProvider;
import org.be.auth.dto.LoginRequest;
import org.be.auth.dto.RegisterRequest;
import org.be.auth.dto.TokenResponse;
import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        public AuthService(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider) {
                this.userRepository = userRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtTokenProvider = jwtTokenProvider;
        }

        // 회원가입 로직
        public void register(RegisterRequest request) {
                if (userRepository.existsByUserId(request.getUserId())) {
                        throw new RuntimeException("이미 존재하는 아이디입니다.");
                }
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("이미 존재하는 이메일입니다.");
                }

                String encodedPassword = passwordEncoder.encode(request.getPassword());
                User user = new User(
                        request.getUserId(),
                        request.getUsername(),
                        request.getEmail(),
                        encodedPassword,
                        "ROLE_USER"
                );
                userRepository.save(user);
        }

        // 로그인 로직 (JWT 발급)
        public TokenResponse login(LoginRequest request) {
                Optional<User> userOptional = userRepository.findByUserId(request.getUserId());

                if (userOptional.isEmpty()) {
                        throw new RuntimeException("사용자를 찾을 수 없습니다.");
                }

                User user = userOptional.get();
                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new RuntimeException("비밀번호가 일치하지 않습니다.");
                }

                String token = jwtTokenProvider.generateToken(user.getUserId());
                return new TokenResponse(token);
        }

        // 토큰 검증
        public boolean validateToken(String token) {
                return jwtTokenProvider.validateToken(token);
        }
}