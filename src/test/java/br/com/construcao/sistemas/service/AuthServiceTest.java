package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.login.LoginRequest;
import br.com.construcao.sistemas.controller.dto.response.login.TokenResponse;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @InjectMocks
    private AuthService service;

    private static User baseUser(Long id, String email, String encodedPwd,
                                 boolean enabled, boolean locked, Integer failedLogins, Instant lastFailureAt) {
        User u = new User();
        u.setId(id);
        u.setName("User " + id);
        u.setEmail(email);
        u.setPassword(encodedPwd);
        u.setEnabled(enabled);
        u.setLocked(locked);
        u.setFailedLogins(failedLogins);
        u.setLastFailureAt(lastFailureAt);
        return u;
    }

    @Nested
    @DisplayName("loginLocal(LoginRequest, String)")
    class LoginLocalTests {

        @Test
        @DisplayName("Deve autenticar com sucesso, resetar failedLogins e gerar tokens")
        void loginLocal_shouldAuthenticate_andResetFails_andReturnTokens() {
            // Given
            LoginRequest req = new LoginRequest();
            req.setEmail("alice@mail.com");
            req.setPassword("plain");

            User u = baseUser(
                    10L,
                    "alice@mail.com",
                    "{enc}plain",
                    true,
                    false,
                    3,
                    Instant.now().minusSeconds(60));

            when(users.findByEmail("alice@mail.com")).thenReturn(Optional.of(u));
            when(encoder.matches("plain", "{enc}plain")).thenReturn(true);
            when(users.save(u)).thenReturn(u);
            when(jwt.generateAccess(u)).thenReturn("access-token");
            when(jwt.generateRefresh(u)).thenReturn("refresh-token");

            // When
            TokenResponse tokens = service.loginLocal(req, "127.0.0.1");

            // Then
            assertNotNull(tokens);
            assertEquals("access-token", tokens.getAccessToken());
            assertEquals("refresh-token", tokens.getRefreshToken());
            assertEquals(0, u.getFailedLogins());

            InOrder inOrder = inOrder(users, encoder, jwt);
            inOrder.verify(users).findByEmail("alice@mail.com");
            inOrder.verify(encoder).matches("plain", "{enc}plain");
            inOrder.verify(users).save(u);
            inOrder.verify(jwt).generateAccess(u);
            inOrder.verify(jwt).generateRefresh(u);

            verifyNoMoreInteractions(users, encoder, jwt);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException 'Credenciais inválidas' quando usuário não encontrado")
        void loginLocal_shouldThrow_whenUserNotFound() {
            // Given
            LoginRequest req = new LoginRequest();
            req.setEmail("missing@mail.com");
            req.setPassword("x");

            when(users.findByEmail("missing@mail.com")).thenReturn(Optional.empty());

            // When / Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.loginLocal(req, "127.0.0.1"));
            assertEquals("Credenciais inválidas", ex.getMessage());

            verify(users).findByEmail("missing@mail.com");
            verifyNoMoreInteractions(users);
            verifyNoInteractions(encoder, jwt);
        }

        @Test
        @DisplayName("Deve lançar RuntimeException 'Conta bloqueada' quando usuário está locked")
        void loginLocal_shouldThrow_whenUserLocked() {
            // Given
            LoginRequest req = new LoginRequest();
            req.setEmail("lock@mail.com");
            req.setPassword("any");

            User u = baseUser(11L, "lock@mail.com", "{enc}zzz", true, true, 5, Instant.now());
            when(users.findByEmail("lock@mail.com")).thenReturn(Optional.of(u));

            // When / Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.loginLocal(req, "127.0.0.1"));
            assertEquals("Conta bloqueada", ex.getMessage());

            verify(users).findByEmail("lock@mail.com");
            verifyNoMoreInteractions(users);
            verifyNoInteractions(encoder, jwt);
        }

        @Test
        @DisplayName("Deve incrementar failedLogins (partindo de null→1), setar lastFailureAt e lançar mensagem com tentativas restantes")
        void loginLocal_shouldIncreaseFailsFromNull_andSetLastFailure_andThrowWithRemainingAttempts() {
            // Given
            LoginRequest req = new LoginRequest();
            req.setEmail("bob@mail.com");
            req.setPassword("wrong");

            User u = baseUser(12L, "bob@mail.com", "{enc}correct", true, false, null, null);
            when(users.findByEmail("bob@mail.com")).thenReturn(Optional.of(u));
            when(encoder.matches("wrong", "{enc}correct")).thenReturn(false);

            ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
            when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When / Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.loginLocal(req, "127.0.0.1"));

            assertTrue(ex.getMessage().startsWith("Senha incorreta. Tentativas restantes:"), "Mensagem deve conter tentativas restantes");
            // MAX_FAILS = 5, agora fails = 1 => restantes = 4
            assertTrue(ex.getMessage().contains("4"), "Restantes devem ser 4");

            verify(users).findByEmail("bob@mail.com");
            verify(encoder).matches("wrong", "{enc}correct");
            verify(users).save(savedCaptor.capture());
            verifyNoMoreInteractions(users, encoder);
            verifyNoInteractions(jwt);

            User saved = savedCaptor.getValue();
            assertEquals(1, saved.getFailedLogins());
            assertNotNull(saved.getLastFailureAt());
            assertFalse(saved.isLocked());
        }

        @Test
        @DisplayName("Deve incrementar failedLogins, continuar desbloqueado e lançar mensagem com tentativas restantes")
        void loginLocal_shouldIncreaseFails_andRemainUnlocked_andThrowWithRemainingAttempts() {
            // Given
            LoginRequest req = new LoginRequest();
            req.setEmail("carol@mail.com");
            req.setPassword("wrong");

            User u = baseUser(13L, "carol@mail.com", "{enc}secret", true, false, 2, Instant.now().minusSeconds(120));
            when(users.findByEmail("carol@mail.com")).thenReturn(Optional.of(u));
            when(encoder.matches("wrong", "{enc}secret")).thenReturn(false);

            ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
            when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When / Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.loginLocal(req, "127.0.0.1"));

            assertTrue(ex.getMessage().contains("Tentativas restantes: 2"));

            verify(users).findByEmail("carol@mail.com");
            verify(encoder).matches("wrong", "{enc}secret");
            verify(users).save(savedCaptor.capture());
            verifyNoMoreInteractions(users, encoder);
            verifyNoInteractions(jwt);

            User saved = savedCaptor.getValue();
            assertEquals(3, saved.getFailedLogins());
            assertNotNull(saved.getLastFailureAt());
            assertFalse(saved.isLocked());
        }

        @Test
        @DisplayName("Deve atingir o limite (5), bloquear a conta e lançar 'Conta bloqueada'")
        void loginLocal_shouldLockAccount_whenReachMaxFails() {
            // Given
            LoginRequest req = new LoginRequest();
            req.setEmail("dave@mail.com");
            req.setPassword("wrong");

            User u = baseUser(14L, "dave@mail.com", "{enc}secret", true, false, 4, Instant.now().minusSeconds(300));
            when(users.findByEmail("dave@mail.com")).thenReturn(Optional.of(u));
            when(encoder.matches("wrong", "{enc}secret")).thenReturn(false);

            ArgumentCaptor<User> savedCaptor = ArgumentCaptor.forClass(User.class);
            when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When / Then
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.loginLocal(req, "127.0.0.1"));
            assertEquals("Conta bloqueada", ex.getMessage());

            verify(users).findByEmail("dave@mail.com");
            verify(encoder).matches("wrong", "{enc}secret");
            verify(users).save(savedCaptor.capture());
            verifyNoMoreInteractions(users, encoder);
            verifyNoInteractions(jwt);

            User saved = savedCaptor.getValue();
            assertEquals(5, saved.getFailedLogins());
            assertTrue(saved.isLocked(), "A conta deve estar bloqueada ao atingir 5 falhas");
            assertNotNull(saved.getLastFailureAt());
        }
    }
}

