package br.com.construcao.sistemas.controller.dto.request.incident;

import br.com.construcao.sistemas.model.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentRequest {
    
    private String location;
    private IncidentStatus incidentStatus;
    private Long assignedUserId;
    private String notes;
}