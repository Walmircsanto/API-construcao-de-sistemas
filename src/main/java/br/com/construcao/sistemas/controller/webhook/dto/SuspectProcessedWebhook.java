package br.com.construcao.sistemas.controller.webhook.dto;

import lombok.Data;

@Data
public class SuspectProcessedWebhook {
    private Long suspectId;
    private String jobId;
    private String status;
    private String message;
    private Long faceId;
    private String s3Path;
}
