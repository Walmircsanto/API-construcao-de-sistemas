package br.com.construcao.sistemas.controller.dto.request.user;

import br.com.construcao.sistemas.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank
    private String name;

    @Email
    private String email;

    @Size(min=6, max=15)
    private String password;

    private Role role;
}
