package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.incident.CreateIncidentRequest;
import br.com.construcao.sistemas.controller.dto.request.incident.UpdateIncidentRequest;
import br.com.construcao.sistemas.controller.dto.response.incident.IncidentResponse;
import br.com.construcao.sistemas.controller.dto.response.page.PageResponse;
import br.com.construcao.sistemas.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/nexus/incidents")
@Tag(name = "Incidents", description = "Endpoints para gerenciamento de incidentes")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @Operation(
            summary = "Cria um novo incidente",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Incidente criado com sucesso"),
                    @ApiResponse(responseCode = "400", description = "Dados inválidos"),
                    @ApiResponse(responseCode = "404", description = "Suspeito ou imagem não encontrados")
            }
    )
    @PostMapping
    public ResponseEntity<Void> createIncident(@Valid @RequestBody CreateIncidentRequest request) {
        incidentService.createIncident(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "Lista incidentes com paginação",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Lista de incidentes retornada",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> listIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<IncidentResponse> incidents = incidentService.findAll(PageRequest.of(page, Math.min(size, 100)));
        return ResponseEntity.ok(incidents);
    }

    @Operation(
            summary = "Busca um incidente pelo ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Incidente encontrado",
                            content = @Content(schema = @Schema(implementation = IncidentResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Incidente não encontrado")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncident(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.findById(id));
    }

    @Operation(
            summary = "Atualiza um incidente",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Incidente atualizado com sucesso",
                            content = @Content(schema = @Schema(implementation = IncidentResponse.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "Incidente não encontrado")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<IncidentResponse> updateIncident(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncidentRequest request
    ) {
        return ResponseEntity.ok(incidentService.updateIncident(id, request));
    }

    @Operation(
            summary = "Deleta um incidente",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Incidente deletado com sucesso"),
                    @ApiResponse(responseCode = "404", description = "Incidente não encontrado")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncident(@PathVariable Long id) {
        incidentService.deleteIncident(id);
        return ResponseEntity.noContent().build();
    }
}