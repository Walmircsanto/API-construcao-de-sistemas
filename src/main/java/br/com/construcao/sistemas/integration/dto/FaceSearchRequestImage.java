package br.com.construcao.sistemas.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class FaceSearchRequestImage {
    MultipartFile image;
    Integer topK;
}
