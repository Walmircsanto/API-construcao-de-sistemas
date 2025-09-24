package br.com.construcao.sistemas.controller.dto.request.suspect;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSuspectRequest {

    private String name;

    @Min(0)
    private Integer age;

    private String urlImage;

    private String cpf;

    private String description;
}
