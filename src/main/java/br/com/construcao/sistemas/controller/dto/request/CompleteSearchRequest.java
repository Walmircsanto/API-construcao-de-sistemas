package br.com.construcao.sistemas.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompleteSearchRequest {
    
    @NotBlank
    private String requestId;
    
    private Long idSuspect;
    
    @JsonProperty("s3_path")
    private String s3Path;
}