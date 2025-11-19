package br.com.construcao.sistemas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class ControllerGoogle {

    @GetMapping("google/home")
    public ResponseEntity<String> getGoogle(@RequestParam(required = false) String token,
                                           @RequestParam(required = false) String user) {
        if (token != null && user != null) {
            return ResponseEntity.ok(String.format(
                "Login OAuth2 realizado com sucesso!\nUsuário: %s\nToken JWT: %s", 
                user, token
            ));
        }
        return ResponseEntity.ok("Login OAuth2 realizado com sucesso!");
    }
    
    @GetMapping("google/error")
    public ResponseEntity<String> getGoogleError(@RequestParam(required = false) String reason) {
        String message = "Erro no login OAuth2 com Google";
        if (reason != null) {
            message += ": " + reason;
        }
        return ResponseEntity.badRequest().body(message);
    }
}
