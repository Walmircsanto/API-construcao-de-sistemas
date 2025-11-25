package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.emergency.CreateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.request.emergency.UpdateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.response.emergency.EmergencyContactResponse;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.service.EmergencyContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/nexus/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService service;


    @Operation(
            summary = "Cria um novo Contato de Emergência (com imagem opcional)",
            description = "Aceita dados JSON (CreateEmergencyContactRequest) e um arquivo opcional em uma requisição multipart/form-data.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contato criado com sucesso.",
                            content = @Content(schema = @Schema(implementation = EmergencyContactResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos.")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmergencyContactResponse> create(
            @RequestPart("data") CreateEmergencyContactRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(service.create(req, file));
    }


    @Operation(
            summary = "Busca um Contato de Emergência pelo ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contato encontrado com sucesso.",
                            content = @Content(schema = @Schema(implementation = EmergencyContactResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Contato não encontrado.")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @Operation(
            summary = "Lista todos os Contatos de Emergência com paginação",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista paginada de contatos.",
                            content = @Content(schema = @Schema(
                                    implementation = Page.class,
                                    subTypes = EmergencyContactResponse.class
                            ))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<Page<EmergencyContactResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @Operation(
            summary = "Atualiza um Contato de Emergência (com imagem opcional)",
            description = "Aceita dados JSON (UpdateEmergencyContactRequest) e um novo arquivo de imagem opcional. Usa multipart/form-data.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Contato atualizado com sucesso.",
                            content = @Content(schema = @Schema(implementation = EmergencyContactResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos."),
                    @ApiResponse(responseCode = "404", description = "Contato não encontrado.")
            }
    )
    @PutMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<EmergencyContactResponse> update(
            @PathVariable Long id,
            @RequestPart("data") UpdateEmergencyContactRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(service.update(id, req, file));
    }

    @Operation(
            summary = "Exclui um Contato de Emergência pelo ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Contato excluído com sucesso (No Content)."),
                    @ApiResponse(responseCode = "404", description = "Contato não encontrado.")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lista as imagens associadas a um Contato de Emergência",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de imagens retornada com sucesso.",
                            content = @Content(schema = @Schema(implementation = List.class, subTypes = ImageResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Contato não encontrado.")
            }
    )
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> listImages(@PathVariable Long id) {
        return ResponseEntity.ok(service.listImages(id));
    }
}
