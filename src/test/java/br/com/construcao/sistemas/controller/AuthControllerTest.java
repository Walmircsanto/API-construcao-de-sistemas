package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.login.LoginRequest;
import br.com.construcao.sistemas.controller.dto.request.user.FcmUpdateRequest;
import br.com.construcao.sistemas.controller.dto.response.login.AuthResponse;
import br.com.construcao.sistemas.service.AuthService;
import br.com.construcao.sistemas.service.UserService;
import br.com.construcao.sistemas.util.helpers.AuthUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;
    @Mock
    private UserService userService;
    @Mock
    private AuthUserResolver authUser;
    @InjectMocks
    private AuthController controller;

    @BeforeEach
    void setup() {
        controller = new AuthController(authService, userService, authUser);
    }

    @Test
    void testLogin() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("123456");

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        AuthResponse mockResponse = new AuthResponse();
        mockResponse.setAccessToken("token123");
        mockResponse.setRefreshToken("refresh123");

        when(authService.loginLocal(req, "127.0.0.1")).thenReturn(mockResponse);

        ResponseEntity<AuthResponse> response = controller.login(req, servletRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token123", Objects.requireNonNull(response.getBody()).getAccessToken());
        assertEquals("refresh123", response.getBody().getRefreshToken());

        verify(servletRequest).setAttribute("LOG_USER_EMAIL", "user@test.com");
        verify(authService).loginLocal(req, "127.0.0.1");
    }

    @Test
    void testLogout() {
        when(authUser.currentUserId()).thenReturn(42L);

        ResponseEntity<Void> response = controller.logout();

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(userService).clearFcmToken(42L);
    }

    @Test
    void testUpdateFcm() {
        FcmUpdateRequest req = new FcmUpdateRequest();
        req.setToken("token123");

        when(authUser.currentUserId()).thenReturn(42L);

        ResponseEntity<Void> response = controller.updateFcm(req);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(userService).updateFcmToken(42L, "token123");
    }

    @Test
    void testUpdateFcm_InvalidToken_ShouldThrow() {
        FcmUpdateRequest req = new FcmUpdateRequest();
        req.setToken("");

        when(authUser.currentUserId()).thenReturn(42L);

        doThrow(new IllegalArgumentException("Token inválido"))
                .when(userService).updateFcmToken(42L, "");

        assertThrows(IllegalArgumentException.class, () -> controller.updateFcm(req));

        verify(userService).updateFcmToken(42L, "");
    }
}