package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.response.notification.NotificationResponse;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.UserRepository;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Log4j2
public class PushNotificationService {

    private final FirebaseMessaging firebase;
    private final UserRepository users;

    @Value("${notifications.fcm.chunk-size:500}")
    private int chunkSize;

    public NotificationResponse sendToUserIds(Collection<Long> userIds, String title, String body, Map<String, String> data) {
        List<String> tokens = users.findAllById(userIds).stream()
                .map(User::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .toList();

        int requested = tokens.size(), success = 0, failure = 0;

        for (String tk : tokens) {
            Message.Builder mb = Message.builder()
                    .setToken(tk)
                    .setNotification(Notification.builder().setTitle(title).setBody(body).build());
            if (data != null && !data.isEmpty()) mb.putAllData(data);

            try {
                firebase.send(mb.build());
                success++;
            } catch (FirebaseMessagingException e) {
                failure++;
                if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    users.findByFcmToken(tk).ifPresent(u -> {
                        u.setFcmToken(null);
                        u.setFcmTokenUpdatedAt(Instant.now());
                        users.save(u);
                    });
                }
                log.warn("FCM single send failed for token {}: {}", tk, e.getMessage(), e);
            }
        }
        return new NotificationResponse(requested, success, failure);
    }


    public void sendToTopic(String topic, String title, String body, Map<String, String> data) {
        Message.Builder mb = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build());
        if (data != null && !data.isEmpty()) mb.putAllData(data);
        try {
            firebase.send(mb.build());
        } catch (FirebaseMessagingException e) {
        }
    }
}
