package org.be.auth.service;

import lombok.RequiredArgsConstructor;
import org.be.auth.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        System.out.println("Searching user with userId: " + userId);

        return userRepository.findByUserId(userId)
                .map(user -> {
                    return new CustomUserDetails(user);
                })
                .orElseThrow(() -> {
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
                });
    }
}