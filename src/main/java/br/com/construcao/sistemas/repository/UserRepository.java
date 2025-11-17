package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByFcmToken(String token);

    Page<User> findAllByRole(Role role, Pageable pageable);

}
