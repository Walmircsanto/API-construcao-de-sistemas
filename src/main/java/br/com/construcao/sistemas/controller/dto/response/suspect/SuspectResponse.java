package br.com.construcao.sistemas.controller.dto.response.suspect;

import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SuspectResponse {
    private Long id;
    private String name;
    private Integer age;
    private String cpf;
    private String description;

    private List<ImageResponse> images = new ArrayList<>();
}
