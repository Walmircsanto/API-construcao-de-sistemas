package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.Suspect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuspectRepository extends JpaRepository<Suspect, Long> {

    boolean existsByCpf(String cpf);
    Optional<Suspect> findByCpf(String cpf);

}
