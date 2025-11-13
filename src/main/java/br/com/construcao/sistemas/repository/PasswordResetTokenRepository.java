package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByTokenHashAndUsedFalse(String tokenHash );
}
