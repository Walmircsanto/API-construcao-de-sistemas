package br.com.construcao.sistemas.integration.dto.suspect;

import br.com.construcao.sistemas.model.enums.SuspectStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ResponseSearchSuspect {
    private String name;
    private LocalDate birthday;
    private SuspectStatus status;
    private String  locality;
    private String dateDetection;
    private String hours;

}
