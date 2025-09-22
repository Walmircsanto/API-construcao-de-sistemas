package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
