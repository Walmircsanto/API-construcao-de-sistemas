package br.com.construcao.sistemas.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AsyncFaceSearchResponse {

    private String message;
    @JsonProperty("job_id")
    private String jobId;
    private String status;
    @JsonProperty("s3_path")
    private String s3Path;
    private String source;
}