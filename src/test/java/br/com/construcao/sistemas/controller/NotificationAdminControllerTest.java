package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import br.com.construcao.sistemas.service.NotificationProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationAdminControllerTest {

    @Mock
    private NotificationProducer producer;

    @InjectMocks
    private NotificationAdminController controller;

    @Test
    void testTestUsers() {
        NotificationRequest request = new NotificationRequest();

        ResponseEntity<Void> response = controller.testUsers(request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        verify(producer, times(1)).enqueueToUsers(request);
    }

    @Test
    void testTestTopic() {
        String topic = "news";
        NotificationRequest request = new NotificationRequest();

        ResponseEntity<Void> response = controller.testTopic(topic, request);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals(topic, request.getTopic());
        verify(producer, times(1)).enqueueToTopic(request);
    }
}