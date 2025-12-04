package br.com.construcao.sistemas.integration.dto;

import lombok.Data;

@Data
public class FaceRegisterResponse {
    private String jobId;
    private String status;
    private String message;
    private Long suspectId;
    private String s3Path;
}
