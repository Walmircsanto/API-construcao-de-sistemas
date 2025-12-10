package br.com.construcao.sistemas.controller.webhook;

import br.com.construcao.sistemas.controller.dto.request.CompleteSearchRequest;
import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import br.com.construcao.sistemas.controller.dto.response.SearchResultResponse;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.controller.webhook.dto.SuspectProcessedWebhook;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.EnumProcessingStatus;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.repository.SuspectRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import br.com.construcao.sistemas.service.NotificationProducer;
import br.com.construcao.sistemas.service.SearchResultService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nexus/webhooks")
@RequiredArgsConstructor
@Log4j2
public class FaceProcessingWebhookController {

    private final SuspectRepository suspectRepository;
    private final NotificationProducer notificationProducer;
    private final UserRepository userRepository;
    private final SearchResultService searchResultService;

    @Value("${aws.region}")
    private String awsRegion;

    @PostMapping("/suspect-processed")
    public ResponseEntity<Void> handleSuspectProcessed(@RequestBody SuspectProcessedWebhook webhook) {

        Suspect suspect = suspectRepository.findById(webhook.getSuspectId())
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));

        if ("completed".equals(webhook.getStatus())) {
            suspect.setFaceProcessingStatus(EnumProcessingStatus.COMPLETO);
            suspect.setFaceProcessingJobId(webhook.getJobId());
            suspectRepository.save(suspect);
            System.out.println("ENVIANDO NOTIFICACAO!");
            enviarNotificacao(suspect, true, webhook.getS3Path());
        } else if ("failed".equals(webhook.getStatus())) {
            suspect.setFaceProcessingStatus(EnumProcessingStatus.FALHOU);
            suspectRepository.save(suspect);
            enviarNotificacao(suspect, false, null);
        }

        return ResponseEntity.ok().build();
    }

    private void enviarNotificacao(Suspect suspect, boolean sucesso, String s3Path) {

        System.out.println("NOTIFICACAO RECEBIDA");
        List<User> users = userRepository.findByStatus(EnumStatus.ATIVO);

        if (users.isEmpty()) {
            return;
        }

        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        NotificationRequest notification = new NotificationRequest();

        System.out.println("NOTIFICACAO RECEBIDA: " + userIds);

        if (sucesso) {
            notification.setTitle("Novo Suspeito Registrado");
            notification.setBody(String.format(
                    "Suspeito %s foi cadastrado com sucesso no sistema de reconhecimento facial.",
                    suspect.getName()
            ));

            if (s3Path != null && !s3Path.isEmpty()) {
                String imageUrl = s3Path;
                notification.setImage(imageUrl);
            }

        } else {
            notification.setTitle("Erro ao Registrar Suspeito");
            notification.setBody(String.format(
                    "Houve um erro ao cadastrar o suspeito %s no sistema de reconhecimento facial.",
                    suspect.getName()
            ));
            notification.setImage(null);
        }

        notification.setTarget("SUSPECT");
        notification.setId(suspect.getCpf());
        notification.setAction("refresh_list");
        notification.setUserIds(userIds);

        notificationProducer.enqueueToUsers(notification);

        System.out.println("NOTIFICACAO RECEBIDA: EnQUEUE" );
    }

    private String convertS3PathToUrl(String s3Path) {
        if (s3Path == null || !s3Path.startsWith("s3://")) {
            return null;
        }

        try {
            String pathWithoutPrefix = s3Path.substring(5);

            String[] parts = pathWithoutPrefix.split("/", 2);
            if (parts.length != 2) {
                return null;
            }

            String bucket = parts[0];
            String key = parts[1];

            String region = awsRegion;
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);

        } catch (Exception e) {
            log.error("Erro ao converter S3 path para URL: {}", s3Path, e);
            return null;
        }
    }

    @Operation(
            summary = "Webhook para receber resultado da busca assíncrona do Python",
            description = "Endpoint chamado pelo Python quando o processamento da busca é finalizado."
    )
    @PostMapping("/complete-search")
    public ResponseEntity<Void> completeSearch(@Valid @RequestBody CompleteSearchRequest request) {
        searchResultService.completeSearch(request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Consulta resultado da busca assíncrona",
            description = "Endpoint para polling do resultado da busca usando requestId."
    )
    @GetMapping("/search-result/{requestId}")
    public ResponseEntity<SearchResultResponse> getSearchResult(@PathVariable String requestId) {
        return ResponseEntity.ok(searchResultService.getSearchResult(requestId));
    }
}
