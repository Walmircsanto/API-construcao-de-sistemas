package br.com.construcao.sistemas.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FaceRegisterRequest {
    @JsonProperty("suspect_id")
    private Long suspectId;

    @JsonProperty("s3_path")
    private String imagePath;

    @JsonProperty("metadata")
    private String metadata;
}
