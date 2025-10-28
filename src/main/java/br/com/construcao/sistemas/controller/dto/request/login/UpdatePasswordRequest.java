package br.com.construcao.sistemas.controller.dto.request.login;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequest {

    private String currentPassword;

    @Size(min=6, max=15)
    private String newPassword;
}
