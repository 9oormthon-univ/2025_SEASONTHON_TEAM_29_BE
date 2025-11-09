package com.wedit.backend.common.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account.path}")
    private String SERVICE_ACCOUNT_PATH;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("FirebaseApp 빈 생성을 시작합니다.");

        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();

        // [핵심] 이미 초기화된 앱이 있는지 확인합니다.
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    log.info("이미 초기화된 FirebaseApp을 반환합니다.");
                    return app;
                }
            }
        }

        // 초기화된 앱이 없을 때만 아래 로직을 실행합니다.
        log.info("FirebaseApp을 새로 초기화합니다.");
        ClassPathResource resource = new ClassPathResource(SERVICE_ACCOUNT_PATH);

        if (!resource.exists()) {
            throw new IOException("Firebase 키 파일을 찾을 수 없습니다. 경로: " + SERVICE_ACCOUNT_PATH);
        }

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            return FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            log.error("Firebase 초기화 중 에러 발생", e);
            throw e;
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
