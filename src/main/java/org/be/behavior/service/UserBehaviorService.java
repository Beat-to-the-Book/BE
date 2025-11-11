package org.be.behavior.service;

import org.be.auth.model.User;
import org.be.auth.repository.UserRepository;
import org.be.behavior.dto.UserBehaviorRequest;
import org.be.behavior.dto.UserBehaviorResponse;
import org.be.behavior.model.UserBehavior;
import org.be.behavior.repository.UserBehaviorRepository;
import org.springframework.stereotype.Service;

@Service
public class UserBehaviorService {

    private final UserRepository userRepository;
    private final UserBehaviorRepository userBehaviorRepository;

    public UserBehaviorService(UserRepository userRepository,
                               UserBehaviorRepository userBehaviorRepository) {
        this.userRepository = userRepository;
        this.userBehaviorRepository = userBehaviorRepository;
    }

    public UserBehaviorResponse saveUserBehavior(String userId, UserBehaviorRequest request) {
        User user = userRepository.findByUserId(userId).orElseThrow();
        UserBehavior log = UserBehavior.builder()
                .user(user)
                .bookId(request.getBookId())
                .stayTime(request.getStayTime())
                .scrollDepth(request.getScrollDepth())
                .timestamp(request.getTimestamp())
                .build();

        UserBehavior saved = userBehaviorRepository.save(log);

        return UserBehaviorResponse.builder()
                .id(saved.getId())
                .userId(user.getUserId())
                .username(user.getUsername())
                .bookId(saved.getBookId())
                .stayTime(saved.getStayTime())
                .scrollDepth(saved.getScrollDepth())
                .timestamp(saved.getTimestamp())
                .build();
    }
}
