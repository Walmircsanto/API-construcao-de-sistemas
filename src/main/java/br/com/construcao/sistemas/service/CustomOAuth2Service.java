package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.AuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2Service extends DefaultOAuth2UserService {


    @Autowired
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // Carrega os dados padrão do usuário do Google
        OAuth2User oAuth2User = super.loadUser(userRequest);
        CreateUserRequest user =  new CreateUserRequest();

        // Extrai informações do usuário (nome, email, etc.)
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");



        // Aqui você pode:
        // - Salvar no banco (se for a primeira vez)
        if(!this.userService.userExistByEmail(email)){
          user.setName(name);
          user.setEmail(email);
          user.setImgProfile(picture);
          user.setAuthProvider(AuthProvider.GOOGLE);

          this.userService.create(user);
        }


        System.out.println("Usuário logado com " + provider + ": " + email);

        // Retorna o objeto OAuth2User para o Spring continuar o fluxo
        return oAuth2User;
    }
}
