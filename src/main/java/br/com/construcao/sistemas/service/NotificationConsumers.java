package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumers {

    private final PushNotificationService push;

    @RabbitListener(queues = "${notifications.queues.user}")
    public void onUserFanout(NotificationRequest payload) {
        var r = push.sendToUserIds(payload.getUserIds(), payload.getTitle(), payload.getBody(), payload.getData());
        log.info("FCM user fanout: requested={}, success={}, failure={}", r.getRequested(), r.getSuccess(), r.getFailure());
    }


    @RabbitListener(queues = "${notifications.queues.topic}")
    public void onTopic(NotificationRequest payload) {
        if (payload.getTopic() == null || payload.getTopic().isBlank()) return;
        push.sendToTopic(payload.getTopic(), payload.getTitle(), payload.getBody(), payload.getData());
        log.info("FCM topic sent: topic={}", payload.getTopic());
    }

    @RabbitListener(queues = "${notifications.queues.user-dlq}")
    public void onUserDlq(NotificationRequest payload) {
        log.error("Message sent to USER DLQ: {}", payload);
    }

    @RabbitListener(queues = "${notifications.queues.topic-dlq}")
    public void onTopicDlq(NotificationRequest payload) {
        log.error("Message sent to TOPIC DLQ: {}", payload);
    }
}
