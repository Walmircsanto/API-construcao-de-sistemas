package br.com.construcao.sistemas.integration.dto;

import lombok.Data;

@Data
public class FaceMatch {
    private Long face_id;
    private Long suspect_id;
    private Double distance;
    private String metadata;
}
