package br.com.construcao.sistemas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class ControllerGoogle {

    @GetMapping("google/home")
    public ResponseEntity<String> getGoogle() {
        return ResponseEntity.ok("Google");
    }
}
