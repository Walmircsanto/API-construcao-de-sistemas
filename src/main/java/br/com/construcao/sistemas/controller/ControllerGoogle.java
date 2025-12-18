package br.com.construcao.sistemas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@Tag(name = "Autenticação OAuth2", description = "Endpoints de callback/redirect após a autenticação OAuth2 com provedores externos (ex: Google).")
public class ControllerGoogle {


    @Operation(
            summary = "Callback de Sucesso do Login Google/OAuth2",
            description = "Endpoint de redirecionamento após a autenticação bem-sucedida. Geralmente exibe uma mensagem ou realiza um redirecionamento front-end.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login realizado com sucesso. Retorna mensagem e, opcionalmente, detalhes do usuário e token JWT.",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    @GetMapping("google/home")
    public ResponseEntity<String> getGoogle(@RequestParam(required = false) String token,
                                            @RequestParam(required = false) String user) {
        if (token != null && user != null) {
            return ResponseEntity.ok(String.format(
                    "Login OAuth2 realizado com sucesso!\nUsuário: %s\nToken JWT: %s",
                    user, token
            ));
        }
        return ResponseEntity.ok("Login OAuth2 realizado com sucesso!");
    }

    @Operation(
            summary = "Callback de Erro do Login Google/OAuth2",
            description = "Endpoint de redirecionamento em caso de falha na autenticação (ex: usuário negou acesso, erro de configuração).",
            responses = {
                    @ApiResponse(
                            responseCode = "400",
                            description = "Erro no login OAuth2. A razão específica pode ser incluída no corpo da resposta.",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    @GetMapping("google/error")
    public ResponseEntity<String> getGoogleError(@RequestParam(required = false) String reason) {
        String message = "Erro no login OAuth2 com Google";
        if (reason != null) {
            message += ": " + reason;
        }
        return ResponseEntity.badRequest().body(message);
    }
}
