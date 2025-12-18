package br.com.construcao.sistemas.controller.dto.response;

import br.com.construcao.sistemas.controller.dto.response.incident.IncidentResponse;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.model.enums.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobResponse {
    private Long id;
    private UserResponse userRequest;
    private IncidentResponse incident;
    private JobStatus status;
    private String jobIdRequest;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}