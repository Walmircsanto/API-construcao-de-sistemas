package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.Suspect;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuspectRepository extends JpaRepository<Suspect, Long> {
}
