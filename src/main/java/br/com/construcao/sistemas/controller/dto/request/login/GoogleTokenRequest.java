package br.com.construcao.sistemas.controller.dto.request.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleTokenRequest {
    @NotBlank(message = "Token do Google é obrigatório")
    private String googleToken;
}