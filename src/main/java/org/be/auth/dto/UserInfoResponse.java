package org.be.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private String userId;
    private String username;
    private String email;
    private String role;
}
