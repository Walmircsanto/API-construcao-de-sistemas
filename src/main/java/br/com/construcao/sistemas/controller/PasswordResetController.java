package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.resetpassword.PasswordResetRequest;
import br.com.construcao.sistemas.controller.dto.response.resetpassword.PasswordResetConfirm;
import br.com.construcao.sistemas.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/nexus/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService service;

    @PostMapping("/password/reset-request")
    public ResponseEntity<Void> resetRequest(@Valid @RequestBody PasswordResetRequest req,
                                             HttpServletRequest http) {
        service.requestReset(req, http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset-confirm")
    public ResponseEntity<Void> resetConfirm(@Valid @RequestBody PasswordResetConfirm req) {
        service.confirmReset(req);
        return ResponseEntity.noContent().build();
    }
}
