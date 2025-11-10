package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbit;

    @Value("${notifications.queues.user}")
    private String userRoutingKey;

    @Value("${notifications.queues.topic}")
    private String topicRoutingKey;

    public void enqueueToUsers(NotificationRequest payload) {
        if (payload.getUserIds() == null || payload.getUserIds().isEmpty())
            return;
        rabbit.convertAndSend(userRoutingKey, payload);
    }

    public void enqueueToTopic(NotificationRequest payload) {
        if (payload.getTopic() == null || payload.getTopic().isBlank())
            return;
        rabbit.convertAndSend(topicRoutingKey, payload);
    }
}
