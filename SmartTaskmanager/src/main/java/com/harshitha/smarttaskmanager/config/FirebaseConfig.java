package com.harshitha.smarttaskmanager.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @PostConstruct
    public void initFirebase() {
        if (!firebaseEnabled) {
            System.out.println("⚠️ Firebase disabled (firebase.enabled=false). App will run without notifications.");
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (!resource.exists()) {
                System.out.println("⚠️ Firebase service account json not found. Running without Firebase.");
                return;
            }

            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }

                System.out.println("✅ Firebase Initialized Successfully");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Firebase init failed. Running without Firebase. Reason: " + e.getMessage());
        }
    }
}