package br.com.construcao.sistemas.controller.webhook;

import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.controller.webhook.dto.SuspectProcessedWebhook;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.EnumProcessingStatus;
import br.com.construcao.sistemas.model.enums.EnumStatus;
import br.com.construcao.sistemas.repository.SuspectRepository;
import br.com.construcao.sistemas.repository.UserRepository;
import br.com.construcao.sistemas.service.NotificationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nexus/webhooks")
@RequiredArgsConstructor
public class FaceProcessingWebhookController {

    private final SuspectRepository suspectRepository;
    private final NotificationProducer notificationProducer;
    private final UserRepository userRepository;

    @PostMapping("/suspect-processed")
    public ResponseEntity<Void> handleSuspectProcessed(@RequestBody SuspectProcessedWebhook webhook) {

        Suspect suspect = suspectRepository.findById(webhook.getSuspectId())
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));

        if ("completed".equals(webhook.getStatus())) {
            suspect.setFaceProcessingStatus(EnumProcessingStatus.COMPLETO);
        } else if ("failed".equals(webhook.getStatus())) {
            suspect.setFaceProcessingStatus(EnumProcessingStatus.FALHOU);
        }

        suspectRepository.save(suspect);

        if ("completed".equals(webhook.getStatus())) {
            enviarNotificacao(suspect);
        }

        return ResponseEntity.ok().build();
    }

    private void enviarNotificacao(Suspect suspect) {
        List<User> users = userRepository.findByStatus(EnumStatus.ATIVO);

        if (users.isEmpty()) {
            return;
        }

        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        NotificationRequest notification = new NotificationRequest();
        notification.setTitle("Novo Suspeito Registrado");
        notification.setBody(String.format(
                "Suspeito %s foi registrado com sucesso no sistema de reconhecimento facial.",
                suspect.getName()
        ));
        notification.setUserIds(userIds);

        System.out.println("Usuários: " + userIds);
        notificationProducer.enqueueToUsers(notification);
    }
}
