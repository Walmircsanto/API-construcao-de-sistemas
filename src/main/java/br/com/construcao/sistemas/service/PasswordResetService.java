package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.resetpassword.PasswordResetRequest;
import br.com.construcao.sistemas.controller.dto.response.resetpassword.PasswordResetConfirm;
import br.com.construcao.sistemas.controller.exceptions.BadRequestException;
import br.com.construcao.sistemas.exception.UnauthorizedException;
import br.com.construcao.sistemas.model.PasswordResetToken;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.repository.PasswordResetTokenRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    @Transactional
    public void requestReset(PasswordResetRequest req, String ip) {
        String email = req.getEmail().trim().toLowerCase();
        Optional<User> opt = userRepo.findByEmail(email);

        if (opt.isPresent()) {
            User u = opt.get();

            String raw = generateSecureToken(48);
            String hash = sha256Hex(raw);

            PasswordResetToken prt = new PasswordResetToken();
            prt.setUser(u);
            prt.setTokenHash(hash);
            prt.setExpiresAt(Instant.now().plus(Duration.ofHours(2)));
            prt.setRequestedIp(ip);
            tokenRepo.save(prt);

            String link = buildResetLink(raw);
            emailService.sendPasswordReset(u.getEmail(), u.getName(), link, prt.getExpiresAt());
        }

    }

    @Transactional
    public void confirmReset(PasswordResetConfirm req) {
        String raw = req.getToken().trim();
        String hash = sha256Hex(raw);

        PasswordResetToken prt = tokenRepo.findTopByTokenHashAndUsedFalse(hash)
                .orElseThrow(() -> new UnauthorizedException("Token inválido"));

        if (Instant.now().isAfter(prt.getExpiresAt())) {
            throw new UnauthorizedException("Token expirado");
        }

        User u = prt.getUser();
        if (req.getNewPassword().length() < 6) {
            throw new BadRequestException("Senha deve ter pelo menos 6 caracteres");
        }

        u.setPassword(encoder.encode(req.getNewPassword()));
        u.setProvisionalPassword(false);
        u.setProvisionalPasswordExpiresAt(null);
        u.setFailedLogins(0);
        u.setLastPasswordChangeAt(Instant.now());
        userRepo.save(u);

        prt.setUsed(true);
        tokenRepo.save(prt);

    }

    private static String generateSecureToken(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String sha256Hex(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(dig.length * 2);
            for (byte x : dig) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private String buildResetLink(String rawToken) {
        return "http://localhost:8080/api/nexus/auth/password/reset-confirm?token=" + rawToken;
    }
}
