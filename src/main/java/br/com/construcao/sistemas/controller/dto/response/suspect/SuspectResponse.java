package br.com.construcao.sistemas.controller.dto.response.suspect;

import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.model.enums.SuspectStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuspectResponse {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private String cpf;
    private String description;
    private SuspectStatus suspectStatus;
    private List<ImageResponse> images = new ArrayList<>();
}
