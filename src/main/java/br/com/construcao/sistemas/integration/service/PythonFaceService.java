package br.com.construcao.sistemas.integration.service;

import br.com.construcao.sistemas.exception.InternalServerErrorException;
import br.com.construcao.sistemas.integration.dto.FaceRegisterRequest;
import br.com.construcao.sistemas.integration.dto.FaceRegisterResponse;
import br.com.construcao.sistemas.integration.dto.FaceSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PythonFaceService {

    private final RestTemplate restTemplate;

    @Value("${nexus.python.base-url}")
    private String baseUrl;

    public void registrarFaceSuspeito(Long suspectId, String imageUrl) {
        String s3Path = toS3Path(imageUrl);

        FaceRegisterRequest body = new FaceRegisterRequest(
                suspectId,
                s3Path
        );

        try {
            restTemplate.postForEntity(
                    baseUrl + "/faces/register",
                    body,
                    Void.class
            );
        } catch (RestClientException e) {
            throw new InternalServerErrorException(
                    "Falha ao registrar face no serviço Python: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Converte:
     *  https://apijava-qrcode.s3.us-west-1.amazonaws.com/João Gabriel.png_1763379626900
     * em:
     *  s3://apijava-qrcode/João Gabriel.png_1763379626900
     */
    private String toS3Path(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL da imagem está vazia");
        }

        // se já estiver no formato s3://, só retorna
        if (url.startsWith("s3://")) {
            return url;
        }

        // remove o prefixo https:// se existir
        String withoutProtocol = url;
        if (withoutProtocol.startsWith("https://")) {
            withoutProtocol = withoutProtocol.substring("https://".length());
        }

        // exemplo agora:
        // apijava-qrcode.s3.us-west-1.amazonaws.com/João Gabriel.png_1763379626900
        int dotS3Index = withoutProtocol.indexOf(".s3.");
        if (dotS3Index == -1) {
            throw new IllegalArgumentException("URL não está no formato esperado de S3: " + url);
        }

        String bucket = withoutProtocol.substring(0, dotS3Index);

        int firstSlash = withoutProtocol.indexOf('/', dotS3Index);
        if (firstSlash == -1 || firstSlash == withoutProtocol.length() - 1) {
            throw new IllegalArgumentException("URL não contém key após o domínio: " + url);
        }

        String key = withoutProtocol.substring(firstSlash + 1); // "João Gabriel.png_1763379626900"

        return "s3://" + bucket + "/" + key;
    }
}
