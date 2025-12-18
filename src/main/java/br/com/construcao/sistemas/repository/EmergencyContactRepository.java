package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    boolean existsByPhone(String phone);
}
