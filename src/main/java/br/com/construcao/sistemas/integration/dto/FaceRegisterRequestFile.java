package br.com.construcao.sistemas.integration.dto;

import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FaceRegisterRequestFile {

    @JsonProperty("suspect_id")
    private Long suspectId;
    @JsonProperty("image")
    private MultipartFile file;
    @JsonProperty("metadata")
    private CreateSuspectRequest metadata;
}
