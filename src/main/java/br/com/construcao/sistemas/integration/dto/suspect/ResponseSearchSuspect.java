package br.com.construcao.sistemas.integration.dto.suspect;

import br.com.construcao.sistemas.model.enums.SuspectStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResponseSearchSuspect {
    // Dados do suspeito
    private String name;
    private LocalDate birthday;
    private SuspectStatus status;
    
    // Dados do FaceMatch
    private Long faceId;
    private Long suspectId;
    private Double distance;
    private String metadata;
    private String originalUrl;
    private String processedUrl;
    private String detectionLocation;
    private String detectionDate;
    private String horsDetection;
    
    // ID da imagem salva no banco
    private Long imageId;
}
