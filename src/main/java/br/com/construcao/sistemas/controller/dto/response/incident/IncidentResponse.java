package br.com.construcao.sistemas.controller.dto.response.incident;

import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.model.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {
    
    private Long id;
    private SuspectResponse suspect;
    private String imageUrl;
    private Double score;
    private String location;
    private IncidentStatus incidentStatus;
    private UserResponse assignedUser;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}