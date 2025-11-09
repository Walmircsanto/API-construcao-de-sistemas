package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.config.filters.AccessLogFilter;
import br.com.construcao.sistemas.controller.dto.request.login.LoginRequest;
import br.com.construcao.sistemas.controller.dto.request.user.FcmUpdateRequest;
import br.com.construcao.sistemas.controller.dto.response.login.AuthResponse;
import br.com.construcao.sistemas.service.AuthService;
import br.com.construcao.sistemas.service.UserService;
import br.com.construcao.sistemas.util.helpers.AuthUserResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/nexus/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;
    private final UserService userService;
    private final AuthUserResolver authUser;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req,
                                              HttpServletRequest request) {

        AuthResponse tokens = service.loginLocal(req, request.getRemoteAddr());

        request.setAttribute(AccessLogFilter.ATTR_USER_EMAIL, req.getEmail());

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Long userId = authUser.currentUserId();
        userService.clearFcmToken(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/fcm")
    public ResponseEntity<Void> updateFcm(@RequestBody @Valid FcmUpdateRequest body) {
        Long userId = authUser.currentUserId();
        userService.updateFcmToken(userId, body.getToken());
        return ResponseEntity.noContent().build();
    }
}
