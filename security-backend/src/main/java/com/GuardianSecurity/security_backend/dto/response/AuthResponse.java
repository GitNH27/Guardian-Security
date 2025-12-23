package com.GuardianSecurity.security_backend.dto.response;


public class AuthResponse {
    private String token;
    private UserResponse userResponse;

    public AuthResponse(String token, UserResponse userResponse) {
        this.token = token;
        this.userResponse = userResponse;
    }

    public String getToken() {
        return token;
    }

    public UserResponse getUserResponse() {
        return userResponse;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserResponse(UserResponse userResponse) {
        this.userResponse = userResponse;
    }
}
