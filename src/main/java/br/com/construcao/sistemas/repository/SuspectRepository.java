package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.model.enums.SuspectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SuspectRepository extends JpaRepository<Suspect, Long> {

    boolean existsByCpf(String cpf);

    Optional<Suspect> findByCpf(String cpf);

    @Query("""
            SELECT u FROM Suspect u
            WHERE (:status IS NULL OR u.status = :status)
              AND (
                   :query IS NULL OR :query = '' OR
                   u.name  ILIKE CONCAT('%', :query, '%') OR
                   u.cpf ILIKE CONCAT('%', :query, '%')
              )
            """)
    Page<Suspect> findAllByFilters(@Param("query") String query,
                                   @Param("status") SuspectStatus status,
                                   Pageable pageable);
}
