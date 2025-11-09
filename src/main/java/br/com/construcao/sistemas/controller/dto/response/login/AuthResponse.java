package br.com.construcao.sistemas.controller.dto.response.login;

import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
