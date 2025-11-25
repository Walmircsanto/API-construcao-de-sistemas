package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.model.enums.AuthProvider;
import br.com.construcao.sistemas.model.enums.EnumRole;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Service
public class CustomOAuth2Service extends DefaultOAuth2UserService {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2Service.class);
    
    public CustomOAuth2Service(UserService userService) {
        this.userService = userService;
    }
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        logger.info("=== CustomOAuth2Service.loadUser CHAMADO ===");
        
        try {
            // Carrega os dados padrão do usuário do Google
            OAuth2User oAuth2User = super.loadUser(userRequest);
            
            // Extrai informações do usuário
            String provider = userRequest.getClientRegistration().getRegistrationId();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");

            logger.info("Processando usuário OAuth2 - Provider: {}, Email: {}, Nome: {}", provider, email, name);
            
            // Verifica se o usuário já existe, se não, cria um novo
            if (email != null && !userService.userExistByEmail(email)) {
                CreateUserRequest newUser = new CreateUserRequest();
                newUser.setName(name);
                newUser.setEmail(email);
                newUser.setImgProfile(picture);
                newUser.setAuthProvider(AuthProvider.GOOGLE);
                newUser.setRole(EnumRole.ADMIN);
                
                logger.info("Criando novo usuário OAuth2: {}", email);
                userService.createdUserByGmail(newUser);
                logger.info("Usuário OAuth2 criado com sucesso: {}", email);
            } else {
                logger.info("Usuário OAuth2 já existe: {}", email);
            }

            // Armazena o email no request para o AccessLogFilter
            try {
                ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                if (attr != null) {
                    HttpServletRequest request = attr.getRequest();
                    request.setAttribute("OAUTH2_USER_EMAIL", email);
                    logger.info("Email OAuth2 armazenado no request: {}", email);
                }
            } catch (Exception e) {
                logger.warn("Não foi possível armazenar email no request: {}", e.getMessage());
            }

            return oAuth2User;
            
        } catch (Exception e) {
            logger.error("Erro ao processar usuário OAuth2: {}", e.getMessage(), e);
            throw e;
        }
    }
}
