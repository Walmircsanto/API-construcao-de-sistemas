package br.com.construcao.sistemas.controller.dto.request.incident;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncidentRequest {
    
    @NotNull(message = "Suspect ID is required")
    private Long suspectId;
    
    @NotNull(message = "Image ID is required")
    private Long imageId;
    
    @NotNull(message = "Score is required")
    @Positive(message = "Score must be positive")
    private Double score;
    
    private String location;
    private String notes;
    private String processedUrl;
}