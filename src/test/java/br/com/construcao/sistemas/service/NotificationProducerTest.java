package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationProducerTest {

    @Mock
    private RabbitTemplate rabbit;

    @InjectMocks
    private NotificationProducer producer;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(producer, "userRoutingKey", "notifications.user");
        ReflectionTestUtils.setField(producer, "topicRoutingKey", "notifications.topic");
    }

    @Test
    void testEnviarParaUsuarios_quandoIdsPresentes() {
        NotificationRequest payload = new NotificationRequest();
        payload.setUserIds(List.of(1L, 2L));

        producer.enqueueToUsers(payload);

        verify(rabbit, times(1)).convertAndSend("notifications.user", payload);
    }

    @Test
    void testNaoDeveEnviarParaUsuarios_quandoIdsForemNulos() {
        NotificationRequest payload = new NotificationRequest();
        payload.setUserIds(null);

        producer.enqueueToUsers(payload);

        verifyNoInteractions(rabbit);
    }

    @Test
    void testDeveEnviarParaUsuarios_quandoIdsForemVazios() {
        NotificationRequest payload = new NotificationRequest();
        payload.setUserIds(List.of());

        producer.enqueueToUsers(payload);

        verifyNoInteractions(rabbit);
    }

    @Test
    void testDeveEnviarParaTopico_quandoTopicoPresente() {
        NotificationRequest payload = new NotificationRequest();
        payload.setTopic("promoções");

        producer.enqueueToTopic(payload);

        verify(rabbit, times(1)).convertAndSend("notifications.topic", payload);
    }

    @Test
    void testNaoDeveEnviarParaTopico_quandoTopicoForNulo() {
        NotificationRequest payload = new NotificationRequest();
        payload.setTopic(null);

        producer.enqueueToTopic(payload);

        verifyNoInteractions(rabbit);
    }

    @Test
    void testNaoDeveEnviarParaTopico_quandoTopicoForVazio() {
        NotificationRequest payload = new NotificationRequest();
        payload.setTopic("   ");

        producer.enqueueToTopic(payload);

        verifyNoInteractions(rabbit);
    }
}