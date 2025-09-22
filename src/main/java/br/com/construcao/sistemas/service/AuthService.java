package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.LoginRequest;
import br.com.construcao.sistemas.controller.dto.response.TokenResponse;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.AuthProvider;
import br.com.construcao.sistemas.model.enums.Role;
import br.com.construcao.sistemas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    private static final int MAX_FAILS = 5;

    public TokenResponse loginLocal(LoginRequest req, String ip) {
        User u = users.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (u.isLocked()) throw new RuntimeException("Conta bloqueada");

        if (!encoder.matches(req.getPassword(), u.getPassword())) {
            int fails = u.getFailedLogins() == null ? 0 : u.getFailedLogins();
            fails++;
            u.setFailedLogins(fails);
            u.setLastFailureAt(Instant.now());
            if (fails >= MAX_FAILS) u.setLocked(true);
            users.save(u);
            int restantes = Math.max(0, MAX_FAILS - fails);
            throw new RuntimeException(restantes == 0 ? "Conta bloqueada" :
                    "Senha incorreta. Tentativas restantes: " + restantes);
        }

        u.setFailedLogins(0);
        users.save(u);
        return new TokenResponse(jwt.generateAccess(u), jwt.generateRefresh(u));
    }
}
