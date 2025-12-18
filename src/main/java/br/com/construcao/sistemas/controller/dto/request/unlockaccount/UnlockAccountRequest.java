package br.com.construcao.sistemas.controller.dto.request.unlockaccount;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnlockAccountRequest {
    @NotBlank
    @Email
    private String email;
}
