package br.com.construcao.sistemas.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceSearchRequest {
    @JsonProperty("top_k")
    private Integer topK = 5;

    @JsonProperty("s3_path")
    private String s3Path;
}