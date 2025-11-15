package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.resetpassword.PasswordResetRequest;
import br.com.construcao.sistemas.controller.dto.response.resetpassword.PasswordResetConfirm;
import br.com.construcao.sistemas.controller.exceptions.BadRequestException;
import br.com.construcao.sistemas.exception.UnauthorizedException;
import br.com.construcao.sistemas.model.PasswordResetToken;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.PasswordResetTokenRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordResetTokenRepository tokenRepo;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private EmailService emailService;

    private PasswordResetService service;

    @BeforeEach
    void setup() {

        service = new PasswordResetService(userRepo, tokenRepo, encoder, emailService);
    }

    private String sha256(String input) {
        try {
            Method m = PasswordResetService.class.getDeclaredMethod("sha256Hex", String.class);
            m.setAccessible(true);
            return (String) m.invoke(null, input);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generateFakeRawToken() {
        byte[] b = new byte[48];
        for (int i = 0; i < b.length; i++) b[i] = 1;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Test
    void testRequestReset_GeraTokenEEnviaEmail() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmail("USER@MAIL.COM");

        User user = new User();
        user.setEmail("user@mail.com");
        user.setName("Diego");

        when(userRepo.findByEmail("user@mail.com")).thenReturn(Optional.of(user));

        service.requestReset(req, "127.0.0.1");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepo).save(captor.capture());

        PasswordResetToken prt = captor.getValue();
        assertNotNull(prt.getTokenHash());
        assertEquals(user, prt.getUser());
        assertEquals("127.0.0.1", prt.getRequestedIp());
        assertTrue(prt.getExpiresAt().isAfter(Instant.now()));

        verify(emailService).sendPasswordReset(
                eq("user@mail.com"),
                eq("Diego"),
                contains("http://localhost:8080/api/nexus/auth/password/reset-confirm?token="),
                any()
        );
    }

    @Test
    void testRequestReset_EmailNaoExiste_NaoFazNada() {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmail("user@mail.com");

        when(userRepo.findByEmail("user@mail.com")).thenReturn(Optional.empty());

        service.requestReset(req, "1.1.1.1");

        verify(tokenRepo, never()).save(any());
        verify(emailService, never()).sendPasswordReset(any(), any(), any(), any());
    }

    @Test
    void testConfirmReset_Sucesso() {
        String raw = generateFakeRawToken();
        String hash = sha256(raw);

        User user = new User();
        user.setEmail("u@a.com");

        PasswordResetToken prt = new PasswordResetToken();
        prt.setTokenHash(hash);
        prt.setUser(user);
        prt.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        prt.setUsed(false);

        when(tokenRepo.findTopByTokenHashAndUsedFalse(hash)).thenReturn(Optional.of(prt));
        when(encoder.encode("novaSenha123")).thenReturn("ENCODED");

        PasswordResetConfirm req = new PasswordResetConfirm();
        req.setToken(raw);
        req.setNewPassword("novaSenha123");

        service.confirmReset(req);

        assertEquals("ENCODED", user.getPassword());
        assertFalse(user.isProvisionalPassword());
        assertNull(user.getProvisionalPasswordExpiresAt());
        assertEquals(0, user.getFailedLogins());
        assertNotNull(user.getLastPasswordChangeAt());

        verify(userRepo).save(user);
        assertTrue(prt.isUsed());
        verify(tokenRepo).save(prt);
    }

    @Test
    void testConfirmReset_TokenInvalido() {
        String raw = "AAA";
        String hash = sha256(raw);

        when(tokenRepo.findTopByTokenHashAndUsedFalse(hash)).thenReturn(Optional.empty());

        PasswordResetConfirm req = new PasswordResetConfirm();
        req.setToken(raw);
        req.setNewPassword("123456");

        assertThrows(UnauthorizedException.class, () -> service.confirmReset(req));
    }

    @Test
    void testConfirmReset_TokenExpirado() {
        String raw = generateFakeRawToken();
        String hash = sha256(raw);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setTokenHash(hash);
        prt.setExpiresAt(Instant.now().minusSeconds(5));
        prt.setUsed(false);

        when(tokenRepo.findTopByTokenHashAndUsedFalse(hash)).thenReturn(Optional.of(prt));

        PasswordResetConfirm req = new PasswordResetConfirm();
        req.setToken(raw);
        req.setNewPassword("123456");

        assertThrows(UnauthorizedException.class, () -> service.confirmReset(req));
    }

    @Test
    void testConfirmReset_SenhaMuitoCurta() {
        String raw = generateFakeRawToken();
        String hash = sha256(raw);

        PasswordResetToken prt = new PasswordResetToken();
        prt.setTokenHash(hash);
        prt.setExpiresAt(Instant.now().plusSeconds(1000));
        prt.setUsed(false);
        User user = new User();
        prt.setUser(user);

        when(tokenRepo.findTopByTokenHashAndUsedFalse(hash)).thenReturn(Optional.of(prt));

        PasswordResetConfirm req = new PasswordResetConfirm();
        req.setToken(raw);
        req.setNewPassword("123");

        assertThrows(BadRequestException.class, () -> service.confirmReset(req));
    }
}