package br.com.construcao.sistemas.integration.dto.suspect;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SuspectData {
    private String cpfSuspect;
    private Long suspectId;
}
