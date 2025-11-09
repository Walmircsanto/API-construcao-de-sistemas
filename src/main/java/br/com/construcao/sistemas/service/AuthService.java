package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.login.LoginRequest;
import br.com.construcao.sistemas.controller.dto.response.login.AuthResponse;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.OwnerType;
import br.com.construcao.sistemas.repository.ImageRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final MyModelMapper mapper;
    private final ImageRepository imageRepo;

    private static final int MAX_FAILS = 5;

    @Transactional
    public AuthResponse loginLocal(LoginRequest req, String ip) {
        String email = req.getEmail().trim().toLowerCase();

        User u = users.findByEmail(email)
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
        u.setLastLoginAt(Instant.now());
        if (req.getFcmToken() != null && !req.getFcmToken().isBlank()) {
            u.setFcmToken(req.getFcmToken());
            u.setFcmTokenUpdatedAt(Instant.now());
        }
        users.save(u);

        String access = jwt.generateAccess(u);
        String refresh = jwt.generateRefresh(u);

        UserResponse ur = mapper.mapTo(u, UserResponse.class);

        imageRepo.findFirstByUser_IdAndOwnerType(u.getId(), OwnerType.USER)
                .ifPresent(img -> ur.setProfileImageUrl(img.getUrl()));

        return new AuthResponse(access, refresh, ur);
    }
}
