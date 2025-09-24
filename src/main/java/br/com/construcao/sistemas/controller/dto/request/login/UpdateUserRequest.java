package br.com.construcao.sistemas.controller.dto.request.login;

import br.com.construcao.sistemas.model.enums.Role;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String name;
    @Email
    private String email;
    private Role role;
    private Boolean enabled;
    private Boolean locked;
}
