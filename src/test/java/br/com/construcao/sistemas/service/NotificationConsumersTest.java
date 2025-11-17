package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import br.com.construcao.sistemas.controller.dto.response.notification.NotificationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class NotificationConsumersTest {

    @Mock
    private PushNotificationService push;

    @InjectMocks
    private NotificationConsumers consumers;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDeveEnviarParaUsuarios_quandoForUserFanout() {
        NotificationRequest req = new NotificationRequest();
        req.setUserIds(List.of(1L, 2L));
        req.setTitle("Hello");
        req.setBody("World");
        req.setData(Map.of("k", "v"));

        NotificationResponse result = new NotificationResponse(2, 2, 0);
        when(push.sendToUserIds(any(), any(), any(), any())).thenReturn(result);

        consumers.onUserFanout(req);

        verify(push).sendToUserIds(
                eq(List.of(1L, 2L)),
                eq("Hello"),
                eq("World"),
                eq(Map.of("k", "v"))
        );
    }

    @Test
    void testDeveEnviarParaTopico_quandoTopicoValido() {
        NotificationRequest req = new NotificationRequest();
        req.setTopic("promo");
        req.setTitle("Title");
        req.setBody("Body");
        req.setData(Map.of("x", "y"));

        consumers.onTopic(req);

        verify(push).sendToTopic("promo", "Title", "Body", Map.of("x", "y"));
    }

    @Test
    void testNaoDeveEnviarParaTopico_quandoTopicoVazio() {
        NotificationRequest req = new NotificationRequest();
        req.setTopic("   ");
        consumers.onTopic(req);

        verify(push, never()).sendToTopic(any(), any(), any(), any());
    }

    @Test
    void testNaoDeveEnviarParaTopico_quandoTopicoNulo() {
        NotificationRequest req = new NotificationRequest();
        req.setTopic(null);

        consumers.onTopic(req);

        verify(push, never()).sendToTopic(any(), any(), any(), any());
    }

    @Test
    void testDeveApenasLogar_noUserDlq_semLancarErro() {
        NotificationRequest req = new NotificationRequest();
        req.setTitle("Error User");

        consumers.onUserDlq(req);
    }

    @Test
    void testDeveApenasLogar_noTopicDlq_semLancarErro() {
        NotificationRequest req = new NotificationRequest();
        req.setTitle("Error Topic");

        consumers.onTopicDlq(req);
    }
}