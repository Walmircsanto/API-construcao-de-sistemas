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
import java.util.HashMap;
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

    public NotificationResponse sendToUserIds(
            Collection<Long> userIds,
            String title,
            String body,
            String target,
            String id,
            String action,
            String image,
            Map<String, String> data
    ) {
        List<String> tokens = users.findAllById(userIds).stream()
                .map(User::getFcmToken)
                .filter(t -> t != null && !t.isBlank())
                .toList();

        int requested = tokens.size(), success = 0, failure = 0;

        for (String tk : tokens) {
            Map<String, String> dataPayload = new HashMap<>();
            dataPayload.put("title", title != null ? title : "");
            dataPayload.put("body", body != null ? body : "");
            dataPayload.put("target", target != null ? target : "unknown");
            dataPayload.put("id", id != null ? id : "");
            dataPayload.put("action", action != null ? action : "");
            dataPayload.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

            if (image != null && !image.isBlank()) {
                dataPayload.put("image", image);
            }

            if (data != null && !data.isEmpty()) {
                dataPayload.putAll(data);
            }

            Message message = Message.builder()
                    .setToken(tk)
                    .putAllData(dataPayload)
                    .build();

            try {
                firebase.send(message);
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

    public void sendToTopic(
            String topic,
            String title,
            String body,
            String target,
            String id,
            String action,
            String image,
            Map<String, String> data
    ) {
        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("title", title != null ? title : "");
        dataPayload.put("body", body != null ? body : "");
        dataPayload.put("target", target != null ? target : "unknown");
        dataPayload.put("id", id != null ? id : "");
        dataPayload.put("action", action != null ? action : "");
        dataPayload.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

        if (image != null && !image.isBlank()) {
            dataPayload.put("image", image);
        }

        if (data != null && !data.isEmpty()) {
            dataPayload.putAll(data);
        }

        Message message = Message.builder()
                .setTopic(topic)
                .putAllData(dataPayload)
                .build();

        try {
            firebase.send(message);
            log.info("✅ Notificação enviada para tópico: {}", topic);
        } catch (FirebaseMessagingException e) {
            log.error("❌ Erro ao enviar notificação para tópico {}: {}", topic, e.getMessage(), e);
        }
    }

    public void sendNotificationToUser(String fcmToken, String title, String body, String target, String id) {
        if (fcmToken == null || fcmToken.isBlank()) {
            return;
        }

        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("title", title);
        dataPayload.put("body", body);
        dataPayload.put("target", target);
        dataPayload.put("id", id);
        dataPayload.put("click_action", "FLUTTER_NOTIFICATION_CLICK");

        Message message = Message.builder()
                .setToken(fcmToken)
                .putAllData(dataPayload)
                .build();

        try {
            firebase.send(message);
        } catch (FirebaseMessagingException e) {
            log.error("Erro ao enviar notificação: {}", e.getMessage());
        }
    }

    public void sendNotificationToAll(String title, String body, String target, String id, String imageURL) {
        sendToTopic("notif.topic", title, body, target, id, "REFRESHLIST", imageURL, null);
    }
}
