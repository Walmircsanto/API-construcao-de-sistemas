package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.incident.CreateIncidentRequest;
import br.com.construcao.sistemas.controller.dto.request.incident.UpdateIncidentRequest;
import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import br.com.construcao.sistemas.controller.dto.response.incident.IncidentResponse;
import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.model.Incident;
import br.com.construcao.sistemas.model.Image;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.IncidentStatus;
import br.com.construcao.sistemas.repository.IncidentRepository;
import br.com.construcao.sistemas.repository.ImageRepository;
import br.com.construcao.sistemas.repository.SuspectRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final SuspectRepository suspectRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final MyModelMapper mapper;
    private final NotificationProducer notificationProducer;

    @Transactional
    public void createIncident(CreateIncidentRequest request) {
        Suspect suspect = suspectRepository.findById(request.getSuspectId())
                .orElseThrow(() -> new RuntimeException("Suspect not found"));
        
        Image image = imageRepository.findById(request.getImageId())
                .orElseThrow(() -> new RuntimeException("Image not found"));
    }

    public Page<IncidentResponse> findAll(Pageable pageable) {
        return incidentRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public IncidentResponse findById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        return toResponse(incident);
    }

    @Transactional
    public IncidentResponse updateIncident(Long id, UpdateIncidentRequest request) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        if (request.getLocation() != null) {
            incident.setLocation(request.getLocation());
        }
        if (request.getIncidentStatus() != null) {
            incident.setIncidentStatus(request.getIncidentStatus());
        }
        if (request.getAssignedUserId() != null) {
            User user = userRepository.findById(request.getAssignedUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            incident.setAssignedUser(user);
        }
        if (request.getNotes() != null) {
            incident.setNotes(request.getNotes());
        }

        return toResponse(incidentRepository.save(incident));
    }

    @Transactional
    public void updateStatusIncident(Long id, IncidentStatus status) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        if(status != null) {
         incident.setIncidentStatus(status);
        }

        this.incidentRepository.save(incident);
    }

    @Transactional
    public void deleteIncident(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new RuntimeException("Incident not found");
        }
        incidentRepository.deleteById(id);
    }

    private IncidentResponse toResponse(Incident incident) {
        IncidentResponse response = mapper.mapTo(incident, IncidentResponse.class);
        if (incident.getImage() != null) {
            response.setImageUrl(incident.getImage().getUrl());
        }
        return response;
    }
}
