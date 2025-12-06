package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.request.suspect.UpdateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.dto.response.page.PageResponse;
import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.integration.dto.FaceSearchResponse;
import br.com.construcao.sistemas.integration.dto.suspect.ResponseSearchSuspect;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.service.SuspectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/nexus/suspects")
@Tag(name = "Suspects", description = "Endpoints para gerenciamento de suspeitos")
public class SuspectsController {

    private final SuspectService suspectService;

    public SuspectsController(SuspectService suspectService) {
        this.suspectService = suspectService;
    }


    @Operation(
            summary = "Cria um novo suspeito com dados e, opcionalmente, um arquivo",
            description = "Este endpoint aceita dados JSON (`CreateSuspectRequest`) e um arquivo em uma requisição `multipart/form-data`.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Suspeito criado com sucesso.",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = SuspectResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Dados inválidos fornecidos."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Erro interno do servidor."
                    )
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(
            @Valid @RequestPart("data") CreateSuspectRequest req,
            @RequestPart(name = "image", required = false) MultipartFile file
    ) throws IOException {
        suspectService.create(req, file);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Busca um suspeito pelo ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Suspeito encontrado com sucesso.",
                            content = @Content(schema = @Schema(implementation = SuspectResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Suspeito não encontrado.")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<SuspectResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(suspectService.get(id));
    }

    @Operation(
            summary = "Lista suspeitos com paginação",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de suspeitos retornada.",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<PageResponse<SuspectResponse>> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) EnumStatus status,
            Pageable pageable
    ) {
        Page<SuspectResponse> p = suspectService.list(query, status, pageable);
        return ResponseEntity.ok(PageResponse.of(p));
    }

    @Operation(
            summary = "Atualiza os dados de um suspeito",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Suspeito atualizado com sucesso.",
                            content = @Content(schema = @Schema(implementation = SuspectResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Dados de requisição inválidos."),
                    @ApiResponse(responseCode = "404", description = "Suspeito não encontrado.")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<SuspectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSuspectRequest req
    ) {
        return ResponseEntity.ok(suspectService.update(id, req));
    }

    @Operation(
            summary = "Exclui um suspeito pelo ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Suspeito excluído com sucesso (No Content)."),
                    @ApiResponse(responseCode = "404", description = "Suspeito não encontrado.")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        suspectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Adiciona uma imagem a um suspeito existente",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Imagem adicionada com sucesso.",
                            content = @Content(schema = @Schema(implementation = ImageResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Suspeito não encontrado.")
            }
    )
    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> addImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        ImageResponse img = suspectService.addImage(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(img);
    }

    @Operation(
            summary = "Lista todas as imagens de um suspeito",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de imagens retornada com sucesso.",
                            content = @Content(schema = @Schema(implementation = List.class, subTypes = ImageResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Suspeito não encontrado.")
            }
    )
    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> listImages(@PathVariable Long id) {
        return ResponseEntity.ok(suspectService.listImages(id));
    }


//    @PostMapping("/search-suspect")
//    public ResponseEntity<FaceSearchResponse> buscarSuspeitosPorS3(@RequestBody FaceSearchRequest requestFace){
//        return new ResponseEntity<>(this.suspectService.buscarSuspeitosPorS3(requestFace), HttpStatus.OK );
//    }

    @PostMapping("/search-suspect")
    public ResponseEntity<ResponseSearchSuspect> buscarSuspeitosPorFile(@RequestPart("image") MultipartFile file, @RequestPart("topK") Integer topK) {
        return ResponseEntity.ok(this.suspectService.buscarSuspeitosPorImagem(file, topK));
    }
}
