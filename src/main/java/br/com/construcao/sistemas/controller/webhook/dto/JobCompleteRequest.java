package br.com.construcao.sistemas.controller.webhook.dto;

import lombok.Data;

@Data
public class JobCompleteRequest {
    private String requestId;
    private Long idSuspect;
    private String s3_path;
}