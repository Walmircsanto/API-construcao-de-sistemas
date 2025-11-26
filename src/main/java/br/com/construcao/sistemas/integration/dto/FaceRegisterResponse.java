package br.com.construcao.sistemas.integration.dto;

import lombok.Data;

@Data
public class FaceRegisterResponse {
    private String message;
    private Long face_id;
    private Long suspect_id;
    private String source;
}
