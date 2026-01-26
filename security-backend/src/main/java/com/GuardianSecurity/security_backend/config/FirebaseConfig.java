package com.GuardianSecurity.security_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            // Ensure your JSON file is in src/main/resources/
            ClassPathResource resource = new ClassPathResource("guardian-security-2b0ab-firebase-adminsdk-fbsvc-3b6e780ce9.json");
            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase Admin SDK has been initialized successfully.");
            }
        } catch (IOException e) {
            System.err.println("Error initializing Firebase Admin SDK: " + e.getMessage());
        }
    }
}