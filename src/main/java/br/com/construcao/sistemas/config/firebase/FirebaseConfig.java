package br.com.construcao.sistemas.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp(
            @Value("${firebase.credentials.base64:}") String base64Json,
            @Value("${firebase.credentials.path:}") String filePath
    ) throws IOException {
        GoogleCredentials creds;

        if (!base64Json.isBlank()) {
            byte[] decoded = Base64.getDecoder().decode(base64Json);
            try (InputStream in = new ByteArrayInputStream(decoded)) {
                creds = GoogleCredentials.fromStream(in);
            }
        } else if (!filePath.isBlank()) {
            try (InputStream in = Files.newInputStream(Paths.get(filePath))) {
                creds = GoogleCredentials.fromStream(in);
            }
        } else {
            creds = GoogleCredentials.getApplicationDefault();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(creds)
                .setProjectId("nexus-multiplataforma")
                .build();

        List<FirebaseApp> apps = FirebaseApp.getApps();
        if (apps != null && !apps.isEmpty()) {
            return apps.get(0);
        }
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp app) {
        return FirebaseMessaging.getInstance(app);
    }
}
