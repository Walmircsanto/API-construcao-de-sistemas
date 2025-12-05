package br.com.construcao.sistemas.integration.dto.suspect;

import br.com.construcao.sistemas.model.enums.SuspectStatus;
import lombok.Data;

@Data
public class ResponseSearchSuspect {
    private String name;
    private String birthday;
    private SuspectStatus status;
    private String  locality;
    private String dateDetection;
    private String hours;

}
