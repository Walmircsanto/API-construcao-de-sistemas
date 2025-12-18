package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.unlockaccount.UnlockAccountRequest;
import br.com.construcao.sistemas.service.UnlockAccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/nexus/unlock-request")
@RequiredArgsConstructor
public class UnlockAccountController {

    private final UnlockAccountService unlockAccountService;

    @PostMapping()
    public ResponseEntity<Void> requestUnlock(@Valid @RequestBody UnlockAccountRequest req,
                                              HttpServletRequest http) {
        unlockAccountService.requestUnlock(req.getEmail(), http.getRemoteAddr());
        return ResponseEntity.noContent().build();
    }
}
