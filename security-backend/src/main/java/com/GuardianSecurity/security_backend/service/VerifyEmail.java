package com.GuardianSecurity.security_backend.service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

// Service for verifying email addresses using Spring Mail
@Service
public class VerifyEmail {
    private final JavaMailSender mailSender;

    public VerifyEmail(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Method to generate a verification code
    public String generateVerificationCode() {
        // Simple code generation logic (for demonstration purposes)
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6-digit code
    }

    // Method to send verification email
    public void sendVerificationEmail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nhum5996@gmail.com.com");
        message.setTo(email);
        message.setSubject("Email Verification");
        message.setText("Your verification code is: " + verificationCode);
        mailSender.send(message);
    }
}
