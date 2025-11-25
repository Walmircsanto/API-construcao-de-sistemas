package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.login.UpdatePasswordRequest;
import br.com.construcao.sistemas.controller.dto.request.login.UpdateUserRequest;
import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.model.enums.Role;
import br.com.construcao.sistemas.service.UserService;
import com.google.protobuf.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/nexus/user")
public class UserController {

    private final UserService service;

    public UserController(UserService userService) {
        this.service = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> create(
            @Valid @RequestPart("req") CreateUserRequest req,
            @RequestPart(name = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, file));
    }


    @Operation(
            summary = "Busca usuário por ID",
            description = "Busca usuário por ID  passando o ID na path variable"
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "404",description = "Não foi possivel encontrar o usuário pelo ID fornecido."),
                    @ApiResponse(responseCode = "200",description = "Usuário encontrado com sucesso.")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(
            summary = "Busca Usuários usando Filtros",
            description = "Busca todos os usuários, ou filtrar usuários com filtros.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listagem realizada com sucesso."),
                    @ApiResponse(responseCode = "400", description = "Parametro Inválido.")
            }
    )
    @GetMapping
    public ResponseEntity<Page<UserResponse>> listAllByFilters(@RequestParam(required = false) Role role,
                                                            @RequestParam(required = false) String query,
                                                            @RequestParam(required = false) EnumStatus status,
                                                            Pageable pageable) {
        return ResponseEntity.ok(service.listAllByRole(role, query, status, pageable));
    }

    @Operation(
            summary = "Edita as informações de um usuário",
            description = "Edita as informações de um usuário passando um ID no path variable, uma REQ no request part, e um FILE no request part",
            responses = {
                    @ApiResponse(responseCode = "200",description = "Usuário atualizado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Usuário não existe ou Parametro ID inválido")
            }
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("req") UpdateUserRequest req,
            @RequestPart(name = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(service.update(id, req, file));
    }


    @Operation(
            summary = "Atualiza a senha de um usuário",
            description = "Edita a senha de um usuário passando um ID no path variable e uma REQ no request body",
            responses = {
                    @ApiResponse(responseCode = "204",description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Usuário não existe ou Parametro ID inválido")
            }
    )
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                               @Valid @RequestBody UpdatePasswordRequest req) {
        service.updatePassword(id, req);
        return ResponseEntity.noContent().build();
    }


    @Operation(
            summary = "Deleta um usuário",
            description = "Deleta um usuário passando um ID no path variable",
            responses = {
                    @ApiResponse(responseCode = "204",description = "No content"),
                    @ApiResponse(responseCode = "404", description = "Usuário não existe ou Parametro ID inválido")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
