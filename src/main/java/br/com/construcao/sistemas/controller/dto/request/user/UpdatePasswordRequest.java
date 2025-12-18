package br.com.construcao.sistemas.controller.dto.request.user;

import br.com.construcao.sistemas.util.annotations.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePasswordRequest {

    @NotBlank
    private String currentPassword;

    @ValidPassword
    private String newPassword;

    @NotBlank
    private String confirmNewPassword;
}
