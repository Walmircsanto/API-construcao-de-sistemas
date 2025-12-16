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
import br.com.construcao.sistemas.model.enums.EnumStatus;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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



        Incident incident = Incident.builder()
                .suspect(suspect)
                .score(request.getScore())
                .location(request.getLocation())
                .imageWithBoundingBoxUrl(request.getProcessedUrl())
                .build();

        incident.setIncidentStatus(IncidentStatus.ABERTO);
        incidentRepository.save(incident);

        System.out.println("Ate aqui eu chegooo " + incident.getIncidentStatus());

        enviarNotificacao(incident, suspect, request);
    }

    private void enviarNotificacao(Incident incident, Suspect suspect, CreateIncidentRequest request) {
        List<User> users = userRepository.findByStatus(EnumStatus.ATIVO);
        
        if (users.isEmpty()) {
            return;
        }
        
        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());
        
        NotificationRequest notification = new NotificationRequest();
        notification.setTitle("Novo Incidente Detectado");
        notification.setBody(String.format(
                "Suspeito %s detectado com %.2f%% de confiança em %s",
                suspect.getName(), 
                request.getScore() * 100,
                request.getLocation() != null ? request.getLocation() : "localização não informada"
        ));
        
        if (incident.getImageUrl() != null) {
            notification.setImage(incident.getImageUrl());
        }
        
        notification.setTarget("INCIDENT");
        notification.setId(incident.getId().toString());
        notification.setAction("refresh_list");
        notification.setUserIds(userIds);

        System.out.println("enviando noitificação com o bodyyyy: "+notification.getBody());
        notificationProducer.enqueueToUsers(notification);
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

    @Transactional
    public Incident saveIncident(Incident incident) {
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident createMockIncident() {
        Incident incident = Incident.builder()
                .location("Câmera Simulada")
                .imageUrl("https://mock-s3-url.com/original.jpg")
                .imageWithBoundingBoxUrl("https://mock-s3-url.com/with-box.jpg")
                .build();
        
        return incidentRepository.save(incident);
    }

    @Transactional
    public Incident createMockIncidentWithImages(String imageUrl, String s3Path) {
        Incident incident = Incident.builder()
                .location("Câmera Simulada")
                .imageUrl(imageUrl)
                .imageWithBoundingBoxUrl(s3Path)
                .score(98.2)
                .build();
        
        return incidentRepository.save(incident);
    }

    private IncidentResponse toResponse(Incident incident) {
        IncidentResponse response = mapper.mapTo(incident, IncidentResponse.class);
        if (incident.getImageUrl() != null) {
            response.setImageUrl(incident.getImageWithBoundingBoxUrl());
        }
        return response;
    }
}
