package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.login.LoginRequest;
import br.com.construcao.sistemas.controller.dto.response.login.AuthResponse;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.exception.UnauthorizedException;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.repository.ImageRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private UserRepository users;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwt;

    @Mock
    private MyModelMapper mapper;

    @Mock
    private ImageRepository imageRepo;

    @InjectMocks
    private AuthService service;

    private User user;
    private LoginRequest req;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setEmail("test@email.com");
        user.setEnabled(true);
        user.setLocked(false);
        user.setStatus(EnumStatus.ATIVO);
        user.setPassword("encoded");
        user.setFailedLogins(0);

        req = new LoginRequest();
        req.setEmail("test@email.com");
        req.setPassword("123");
    }

    @Test
    void testUserNotFound() {
        when(users.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> service.loginLocal(req, "127.0.0.1"));
    }

    @Test
    void testUserDisabled() {
        user.setEnabled(false);
        when(users.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(LockedException.class,
                () -> service.loginLocal(req, "127.0.0.1"));
    }

    @Test
    void testUserBlocked() {
        user.setLocked(true);
        user.setStatus(EnumStatus.BLOQUEADO);
        when(users.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(LockedException.class,
                () -> service.loginLocal(req, "127.0.0.1"));
    }

    @Test
    void testInvalidPasswordIncrementsFailCounter() {
        when(users.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> service.loginLocal(req, "127.0.0.1"));

        assertEquals(1, user.getFailedLogins());
        assertNotNull(user.getLastFailureAt());
    }

    @Test
    void testAccountGetsBlockedAfterMaxFails() {
        user.setFailedLogins(4);

        when(users.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> service.loginLocal(req, "127.0.0.1"));

        assertTrue(user.isLocked());
        assertEquals(EnumStatus.BLOQUEADO, user.getStatus());
    }

    @Test
    void testSuccessfulLogin() {
        when(users.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwt.generateAccess(any())).thenReturn("access-token");
        when(jwt.generateRefresh(any())).thenReturn("refresh-token");

        UserResponse ur = new UserResponse();
        when(mapper.mapTo(any(), eq(UserResponse.class))).thenReturn(ur);

        when(imageRepo.findFirstByUser_IdAndOwnerType(anyLong(), any()))
                .thenReturn(Optional.empty());

        AuthResponse res = service.loginLocal(req, "127.0.0.1");

        assertEquals("access-token", res.getAccessToken());
        assertEquals("refresh-token", res.getRefreshToken());
        assertFalse(res.isMustChangePassword());
    }

    @Test
    void testLoginUpdatesFcmToken() {
        req.setFcmToken("token123");

        when(users.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwt.generateAccess(any())).thenReturn("x");
        when(jwt.generateRefresh(any())).thenReturn("y");

        when(mapper.mapTo(any(), eq(UserResponse.class))).thenReturn(new UserResponse());
        when(imageRepo.findFirstByUser_IdAndOwnerType(anyLong(), any()))
                .thenReturn(Optional.empty());

        service.loginLocal(req, "127.0.0.1");

        assertEquals("token123", user.getFcmToken());
        assertNotNull(user.getFcmTokenUpdatedAt());
    }

    @Test
    void testMustChangePasswordWhenExpired() {
        user.setProvisionalPassword(true);
        user.setProvisionalPasswordExpiresAt(Instant.now().minusSeconds(3600));

        when(users.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(encoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwt.generateAccess(any())).thenReturn("a");
        when(jwt.generateRefresh(any())).thenReturn("b");

        when(mapper.mapTo(any(), eq(UserResponse.class))).thenReturn(new UserResponse());
        when(imageRepo.findFirstByUser_IdAndOwnerType(anyLong(), any()))
                .thenReturn(Optional.empty());

        AuthResponse res = service.loginLocal(req, "127.0.0.1");

        assertTrue(res.isMustChangePassword());
    }
}