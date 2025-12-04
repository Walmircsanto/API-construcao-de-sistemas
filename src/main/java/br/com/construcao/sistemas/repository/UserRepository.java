package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.model.enums.EnumRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByFcmToken(String token);

    @Query("""
       SELECT u FROM User u
       WHERE (:role IS NULL OR u.role = :role)
         AND (:status IS NULL OR u.status = :status)
         AND (
              :query IS NULL OR :query = '' OR
              u.name  ILIKE CONCAT('%', :query, '%') OR
              u.email ILIKE CONCAT('%', :query, '%')
         )
       """)
    Page<User> findAllByFilters(@Param("role") EnumRole role,
                                @Param("query") String query,
                                @Param("status") EnumStatus status,
                                Pageable pageable);

    List<User> findByRole(EnumRole role);

    List<User> findByStatus(EnumStatus status);
}
