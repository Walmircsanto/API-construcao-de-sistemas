package br.com.construcao.sistemas.controller.dto.response.image;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponse {
    private Long id;
    private String url;
    private String contentType;
    private Long sizeBytes;
}
