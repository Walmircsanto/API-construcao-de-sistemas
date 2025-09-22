package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.config.AccessLogFilter;
import br.com.construcao.sistemas.controller.dto.request.LoginRequest;
import br.com.construcao.sistemas.controller.dto.response.TokenResponse;
import br.com.construcao.sistemas.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/nexus/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest req,
                                               HttpServletRequest request) {

        TokenResponse tokens = service.loginLocal(req, request.getRemoteAddr());

        request.setAttribute(AccessLogFilter.ATTR_USER_EMAIL, req.getEmail());

        return ResponseEntity.ok(tokens);
    }
}
