package br.com.construcao.sistemas.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
}
