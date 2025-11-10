package br.com.construcao.sistemas.controller.dto.response.notification;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private int requested;
    private int success;
    private int failure;
}
