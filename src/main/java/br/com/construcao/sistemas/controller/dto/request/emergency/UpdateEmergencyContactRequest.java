package br.com.construcao.sistemas.controller.dto.request.emergency;

import br.com.construcao.sistemas.model.enums.ServiceType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateEmergencyContactRequest {
    private String name;
    private String phone;
    private ServiceType serviceType;
}
