package br.com.construcao.sistemas.controller.dto.response.resetpassword;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirm {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
