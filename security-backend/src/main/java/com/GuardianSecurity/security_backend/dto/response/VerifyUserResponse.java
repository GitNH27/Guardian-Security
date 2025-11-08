package com.GuardianSecurity.security_backend.dto.response;

public class VerifyUserResponse {
    private String message;

    public VerifyUserResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
