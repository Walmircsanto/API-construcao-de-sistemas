package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.model.enums.EnumRole;
import br.com.construcao.sistemas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UnlockAccountService {

    private final UserRepository userRepo;
    private final EmailService emailService;

    @Transactional
    public void requestUnlock(String email, String ip) {
        String normalized = email.trim().toLowerCase();

        Optional<User> opt = userRepo.findByEmail(normalized);
        if (opt.isEmpty()) {
            return;
        }

        User user = opt.get();

        if (!user.isLocked() && user.getStatus() != EnumStatus.BLOQUEADO) {
            return;
        }

        List<User> admins = userRepo.findByRole(EnumRole.ADMIN);
        if (admins.isEmpty()) {
            return;
        }

        User admin = admins.get(new Random().nextInt(admins.size()));

        emailService.sendUnlockRequest(
                admin.getEmail(),
                admin.getName(),
                user.getName(),
                user.getEmail(),
                ip
        );
    }
}
