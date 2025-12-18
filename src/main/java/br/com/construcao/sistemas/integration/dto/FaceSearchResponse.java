package br.com.construcao.sistemas.integration.dto;

import lombok.Data;

import java.util.List;

@Data
public class FaceSearchResponse {
    private Long query_face_id;
    private String source;
    private List<FaceMatch> matches;
    private String job_id; // Para processamento assíncrono
    private String status;
}
