package com.GuardianSecurity.security_backend.dto.request;
import com.GuardianSecurity.security_backend.dto.request.RegisterRequest;

import java.io.Serializable;

// DTO SendGrid Verify Email Request
public class UnverifiedUserRequest implements Serializable {
    private RegisterRequest registerRequest;
    private String verificationCode;

    public UnverifiedUserRequest() {
    }

    public UnverifiedUserRequest(RegisterRequest registerRequest, String verificationCode) {
        this.registerRequest = registerRequest;
        this.verificationCode = verificationCode;
    }
    public RegisterRequest getRegisterRequest() {
        return registerRequest;
    }
    public String getVerificationCode() {
        return verificationCode;
    }
    public void setRegisterRequest(RegisterRequest registerRequest) {
        this.registerRequest = registerRequest;
    }
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}