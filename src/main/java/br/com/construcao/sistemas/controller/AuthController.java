package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.config.filters.AccessLogFilter;
import br.com.construcao.sistemas.controller.dto.request.login.LoginRequest;
import br.com.construcao.sistemas.controller.dto.request.user.FcmUpdateRequest;
import br.com.construcao.sistemas.controller.dto.response.login.AuthResponse;
import br.com.construcao.sistemas.service.AuthService;
import br.com.construcao.sistemas.service.UserService;
import br.com.construcao.sistemas.util.helpers.AuthUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/nexus/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação (Local)", description = "Endpoints para Login, Logout e gerenciamento do token de notificação (FCM).")
public class AuthController {
    private final AuthService service;
    private final UserService userService;
    private final AuthUserResolver authUser;


    @Operation(
            summary = "Login de usuário com credenciais locais (e-mail e senha)",
            description = "Autentica o usuário, gera e retorna o token de acesso (JWT).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login bem-sucedido. Retorna o token de autenticação.",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Credenciais inválidas.")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest req,
                                              HttpServletRequest request) {

        AuthResponse tokens = service.loginLocal(req, request.getRemoteAddr());

        request.setAttribute(AccessLogFilter.ATTR_USER_EMAIL, req.getEmail());

        return ResponseEntity.ok(tokens);
    }

    @Operation(
            summary = "Logout do usuário autenticado",
            description = "Limpa o token de notificação FCM associado ao usuário e encerra a sessão lógica.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Logout/Token FCM limpo com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autorizado.")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Long userId = authUser.currentUserId();
        userService.clearFcmToken(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Atualiza o token de notificação Firebase (FCM)",
            description = "Associa um novo token FCM ao usuário logado para recebimento de push notifications.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Token FCM atualizado com sucesso."),
                    @ApiResponse(responseCode = "401", description = "Não autorizado."),
                    @ApiResponse(responseCode = "400", description = "Token FCM inválido.")
            }
    )
    @PostMapping("/fcm")
    public ResponseEntity<Void> updateFcm(@RequestBody @Valid FcmUpdateRequest body) {
        Long userId = authUser.currentUserId();
        userService.updateFcmToken(userId, body.getToken());
        return ResponseEntity.noContent().build();
    }
}
