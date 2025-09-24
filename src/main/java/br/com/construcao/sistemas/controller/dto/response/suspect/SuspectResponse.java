package br.com.construcao.sistemas.controller.dto.response.suspect;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuspectResponse {

    private Long id;

    private String name;

    private int age;

    private String urlImage;

    private String cpf;

    private String description;
}
