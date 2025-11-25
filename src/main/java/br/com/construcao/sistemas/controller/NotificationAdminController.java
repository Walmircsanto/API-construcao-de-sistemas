package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.service.NotificationProducer;
import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;


@RestController
@RequestMapping("api/nexus/admin/notifications")
@RequiredArgsConstructor
@Tag(name = "Notificações (Admin)", description = "Endpoints para teste e envio de notificações via Producer/Queue (Uso Restrito).")
public class NotificationAdminController {

    private final NotificationProducer producer;


    @Operation(
            summary = "Envia uma notificação de teste para todos os usuários",
            description = "Este endpoint enfileira uma notificação para ser enviada a todos os usuários registrados. Retorna 202 ACCEPTED, indicando que a requisição foi aceita para processamento.",
            requestBody = @RequestBody(
                    description = "Payload da notificação contendo título, corpo e dados.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Requisição aceita e notificação enfileirada."),
                    @ApiResponse(responseCode = "400", description = "Dados da notificação inválidos.")
            }
    )
    @PostMapping("/users")
    public ResponseEntity<Void> testUsers(@RequestBody NotificationRequest payload) {
        producer.enqueueToUsers(payload);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Envia uma notificação de teste para um tópico específico",
            description = "Este endpoint enfileira uma notificação para todos os inscritos em um tópico (ex: 'news', 'promotions'). O tópico é extraído do Path Variable.",
            requestBody = @RequestBody(
                    description = "Payload da notificação (o campo 'topic' na requisição será sobrescrito pelo Path Variable).",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "202", description = "Requisição aceita e notificação enfileirada para o tópico."),
                    @ApiResponse(responseCode = "400", description = "Tópico ou dados da notificação inválidos.")
            }
    )
    @PostMapping("/topic/{topic}")
    public ResponseEntity<Void> testTopic(@PathVariable String topic, @RequestBody NotificationRequest body) {
        body.setTopic(topic);
        producer.enqueueToTopic(body);
        return ResponseEntity.accepted().build();
    }
}
