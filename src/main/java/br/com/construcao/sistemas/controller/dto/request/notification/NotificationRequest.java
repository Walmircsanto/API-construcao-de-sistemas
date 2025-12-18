package br.com.construcao.sistemas.controller.dto.request.notification;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private List<Long> userIds;
    private String topic;
    private String title;
    private String body;
    private String image;
    private String target;
    private String id;
    private String action;
    private Map<String, String> data;
}
