package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
}
