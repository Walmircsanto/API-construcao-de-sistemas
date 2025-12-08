package br.com.construcao.sistemas.integration.dto;

import lombok.Data;

@Data
public class FaceMatch {
    private Long face_id;
    private Long suspect_id;
    private Double distance;
    private String metadata;
    private String s3_path; // esse e meu original_url
    private String processed_url;

    private String detection_location;
    private String detection_date;
    private String detection_time;
}
