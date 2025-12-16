package br.com.construcao.sistemas.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchSuspectRequest {
    
    @NotBlank(message = "Localização é obrigatória")
    @Size(max = 50, message = "Localização deve ter no máximo 50 caracteres")
    private String location;
}