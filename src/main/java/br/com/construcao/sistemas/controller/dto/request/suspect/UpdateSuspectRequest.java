package br.com.construcao.sistemas.controller.dto.request.suspect;

import br.com.construcao.sistemas.model.enums.SuspectStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSuspectRequest {

    private String name;
    private LocalDate birthDate;
    private String cpf;
    @Size(max = 2000)
    private String description;
    private SuspectStatus suspectStatus;
}
