package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.resetpassword.PasswordResetRequest;
import br.com.construcao.sistemas.controller.dto.response.resetpassword.PasswordResetConfirm;
import br.com.construcao.sistemas.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetControllerTest {

    @Mock
    private PasswordResetService service;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private PasswordResetController controller;

    @Test
    void testResetRequest() {
        PasswordResetRequest req = new PasswordResetRequest();
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        ResponseEntity<Void> response = controller.resetRequest(req, httpServletRequest);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service, times(1)).requestReset(req, "127.0.0.1");
    }

    @Test
    void testResetConfirm() {
        PasswordResetConfirm req = new PasswordResetConfirm();

        ResponseEntity<Void> response = controller.resetConfirm(req);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service, times(1)).confirmReset(req);
    }
}