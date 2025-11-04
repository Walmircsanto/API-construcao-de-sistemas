package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.Image;
import br.com.construcao.sistemas.model.enums.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByOwnerTypeAndSuspectId(OwnerType ownerType, Long suspectId);
    List<Image> findByOwnerTypeAndEmergencyContactId(OwnerType ownerType, Long emergencyId);
}
