package br.com.construcao.sistemas.config.security;

import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.AuthProvider;
import br.com.construcao.sistemas.model.enums.Role;
import br.com.construcao.sistemas.repository.UserRepository;
import br.com.construcao.sistemas.service.JwtService;
import br.com.construcao.sistemas.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        log.info("OAuth2 authentication success for user: {}", email);
        
        // Busca o usuário no banco
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Gera JWT token
            String token = jwtService.generateAccess(user);
            
            // Redireciona com o token como parâmetro
            String targetUrl = String.format("/api/auth/google/home?token=%s&user=%s", token, user.getName());
            
            log.info("Redirecting OAuth2 user to: {}", targetUrl);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            log.error(": {}", email);
            String emailRegister = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");

            CreateUserRequest newUserRegister = new CreateUserRequest();
            newUserRegister.setName(name);
            newUserRegister.setEmail(email);
            newUserRegister.setImgProfile(picture);
            newUserRegister.setAuthProvider(AuthProvider.GOOGLE);
            newUserRegister.setRole(Role.ADMIN);

            logger.info("Criando novo usuário OAuth2: {}" + newUserRegister.getName());
            userService.createdUserByGmail(newUserRegister);
            getRedirectStrategy().sendRedirect(request, response, "/api/auth/google/error?reason=user_not_found");
        }
    }
}