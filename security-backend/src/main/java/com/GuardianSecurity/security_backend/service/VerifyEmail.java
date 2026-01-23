package com.GuardianSecurity.security_backend.service;
import com.GuardianSecurity.security_backend.dto.request.EmailLogRequest;
import com.GuardianSecurity.security_backend.model.ThreatRecord;
import com.GuardianSecurity.security_backend.repository.ThreatRecordRepository;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import com.GuardianSecurity.security_backend.service.ThreatLogService;

import jakarta.mail.internet.MimeMessage;

// Service for verifying email addresses using Spring Mail
@Service
public class VerifyEmail {
    private final JavaMailSender mailSender;
    public final ThreatLogService threatLogService;
    private final ThreatRecordRepository threatRecordRepository;

    public VerifyEmail(JavaMailSender mailSender, ThreatLogService threatLogService, ThreatRecordRepository threatRecordRepository) {
        this.mailSender = mailSender;
        this.threatLogService = threatLogService;
        this.threatRecordRepository = threatRecordRepository;
    }

    // Method to generate a verification code
    public String generateVerificationCode() {
        // Simple code generation logic (for demonstration purposes)
        return String.valueOf((int)(Math.random() * 900000) + 100000); // 6-digit code
    }

    // Method to send verification email
    public void sendVerificationEmail(String email, String verificationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("nhum5996@gmail.com");
        message.setTo(email);
        message.setSubject("Email Verification");
        message.setText("Your verification code is: " + verificationCode);
        mailSender.send(message);
    }

    // Method to email threat log and photo attachment
    public void emailThreatLog(EmailLogRequest request) {
        // 1. Fetch Log Details
        ThreatRecord record = threatRecordRepository.findById(request.logId())
            .orElseThrow(() -> new RuntimeException("Log not found for ID: " + request.logId()));

        // 2. Generate URLs
        // View URL (Inline)
        String viewUrl = threatLogService.generateSasUrl(record.getPhotoUrl(), false); 
        // Download URL (Forced Attachment)
        String downloadUrl = threatLogService.generateSasUrl(record.getPhotoUrl(), true); 

        // 3. Build HTML Body
        String htmlContent = String.format(
            "<html><body style='font-family: Arial, sans-serif;'>" +
            "<h2> Security Alert: %s Threat</h2>" +
            "<p><strong>Object Detected:</strong> %s</p>" +
            "<p><strong>Camera:</strong> %s</p>" +
            "<p><strong>Timestamp:</strong> %s</p>" +
            "<hr/>" +
            "<img src='%s' style='width: 100%%; max-width: 500px; border-radius: 8px;' />" +
            "<p style='margin-top: 20px;'>" +
            "  <a href='%s' style='background-color: #0078D4; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>View Original</a>" +
            "  &nbsp;" +
            "  <a href='%s' style='background-color: #28a745; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>Download Evidence</a>" +
            "</p>" +
            "</body></html>",
            record.getThreatLevel(), record.getObjectDetected(), record.getCameraTopic(), 
            record.getCreatedAt(), viewUrl, viewUrl, downloadUrl
        );

        // 4. Send Email
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(request.email());
            helper.setSubject("Guardian Alert: " + record.getObjectDetected() + " Detected");
            helper.setText(htmlContent, true);
            helper.setFrom("nhum5996@gmail.com");

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Email dispatch failed: " + e.getMessage());
        }
    }
}
