package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.response.notification.NotificationResponse;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @Mock
    private FirebaseMessaging firebase;
    @Mock
    private UserRepository userRepo;
    private PushNotificationService service;

    @BeforeEach
    void setup() throws Exception {
        service = new PushNotificationService(firebase, userRepo);

        var field = PushNotificationService.class.getDeclaredField("chunkSize");
        field.setAccessible(true);
        field.set(service, 500);
    }

    @Test
    void testSendToUserIds_Sucesso() throws Exception {
        User u1 = new User();
        u1.setFcmToken("token1");
        User u2 = new User();
        u2.setFcmToken("token2");

        when(userRepo.findAllById(Arrays.asList(1L, 2L)))
                .thenReturn(List.of(u1, u2));

        NotificationResponse resp = service.sendToUserIds(
                List.of(1L, 2L),
                "Título",
                "Corpo",
                Map.of("a", "1")
        );

        assertEquals(2, resp.getRequested());
        assertEquals(2, resp.getSuccess());
        assertEquals(0, resp.getFailure());

        verify(firebase, times(2)).send(any(Message.class));
    }

    @Test
    void testSendToUserIds_FalhaComUnregistered_RemoveToken() throws Exception {
        User u = new User();
        u.setId(10L);
        u.setFcmToken("tk123");

        when(userRepo.findAllById(List.of(10L))).thenReturn(List.of(u));

        FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
        when(ex.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);

        when(firebase.send(any(Message.class))).thenThrow(ex);

        when(userRepo.findByFcmToken("tk123")).thenReturn(Optional.of(u));

        NotificationResponse resp = service.sendToUserIds(
                List.of(10L),
                "Título",
                "Corpo",
                null
        );

        assertEquals(1, resp.getRequested());
        assertEquals(0, resp.getSuccess());
        assertEquals(1, resp.getFailure());

        assertNull(u.getFcmToken());
        assertNotNull(u.getFcmTokenUpdatedAt());

        verify(userRepo).save(u);
    }

    @Test
    void testSendToUserIds_FalhaGenerica_NaoRemoveToken() throws Exception {
        User u = new User();
        u.setId(10L);
        u.setFcmToken("tk123");

        when(userRepo.findAllById(List.of(10L))).thenReturn(List.of(u));

        FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
        when(ex.getMessagingErrorCode()).thenReturn(MessagingErrorCode.UNREGISTERED);

        when(firebase.send(any(Message.class))).thenThrow(ex);

        NotificationResponse resp = service.sendToUserIds(
                List.of(10L),
                "Título",
                "Corpo",
                null
        );

        assertEquals(1, resp.getRequested());
        assertEquals(0, resp.getSuccess());
        assertEquals(1, resp.getFailure());

        verify(userRepo, never()).save(any());
    }

    @Test
    void testSendToUserIds_IgnoraUsuariosSemToken() throws FirebaseMessagingException {
        User u1 = new User(); u1.setFcmToken(null);
        User u2 = new User(); u2.setFcmToken("");
        User u3 = new User(); u3.setFcmToken("valid");

        when(userRepo.findAllById(List.of(1L,2L,3L)))
                .thenReturn(List.of(u1, u2, u3));

        NotificationResponse resp = service.sendToUserIds(
                List.of(1L,2L,3L),
                "Título",
                "Corpo",
                null
        );

        assertEquals(1, resp.getRequested());
        assertEquals(1, resp.getSuccess());
        assertEquals(0, resp.getFailure());


        verify(firebase, times(1)).send(any());
    }

    @Test
    void testSendToTopic_Sucesso() throws Exception {
        service.sendToTopic("news", "Hi", "Body", Map.of("x", "1"));
        verify(firebase).send(any(Message.class));
    }

    @Test
    void testSendToTopic_ErroNaoLancaExcecao() throws Exception {
        FirebaseMessagingException ex = mock(FirebaseMessagingException.class);
        when(firebase.send(any(Message.class))).thenThrow(ex);

        assertDoesNotThrow(() ->
                service.sendToTopic("topic", "T", "B", null)
        );
    }
}