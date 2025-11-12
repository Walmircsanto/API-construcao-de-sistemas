package br.com.construcao.sistemas.controller.dto.response.user;

import br.com.construcao.sistemas.model.enums.AuthProvider;
import br.com.construcao.sistemas.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private AuthProvider provider;
    private Boolean enabled;
    private Boolean locked;
    private String profileImageUrl;
}
