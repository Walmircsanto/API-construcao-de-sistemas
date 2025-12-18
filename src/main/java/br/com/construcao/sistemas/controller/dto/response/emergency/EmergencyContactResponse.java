package br.com.construcao.sistemas.controller.dto.response.emergency;

import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.model.enums.ServiceType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContactResponse {
    private Long id;
    private String name;
    private String phone;
    private ServiceType serviceType;
    private List<ImageResponse> images;
}
