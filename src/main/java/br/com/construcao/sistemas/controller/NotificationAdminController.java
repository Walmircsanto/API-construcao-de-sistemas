package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.service.NotificationProducer;
import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/nexus/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationProducer producer;

    @PostMapping("/users")
    public ResponseEntity<Void> testUsers(@RequestBody NotificationRequest payload) {
        producer.enqueueToUsers(payload);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/topic/{topic}")
    public ResponseEntity<Void> testTopic(@PathVariable String topic, @RequestBody NotificationRequest body) {
        body.setTopic(topic);
        producer.enqueueToTopic(body);
        return ResponseEntity.accepted().build();
    }
}
