package br.com.construcao.sistemas.integration.service;

import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.exception.InternalServerErrorException;
import br.com.construcao.sistemas.integration.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PythonFaceService {

    private final RestTemplate restTemplate;

    @Value("${nexus.python.base-url}")
    private String baseUrl;

    public void registrarFaceSuspeito(Long suspectId, String imageUrl, CreateSuspectRequest metadata) {
        String s3Path = toS3Path(imageUrl);

        FaceRegisterRequest body = new FaceRegisterRequest(
                suspectId,
                s3Path
        );

        try {
            System.out.println(baseUrl + " A base url e essa ai ");
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

    public void registrarSuspeitoImagem(Long suspectId, MultipartFile image, String s3Path){

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("suspect_id", suspectId.toString());
            body.add("s3_path", s3Path);

            // Arquivo: MultipartFile -> ByteArrayResource
            ByteArrayResource imageRequest = new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename(); // obrigatório para o Flask reconhecer como arquivo
                }
            };
            body.add("image", imageRequest);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    baseUrl + "/faces/register",
                    request,
                    Void.class
            );


        } catch (RestClientException e) {
            throw new InternalServerErrorException(
                    "Falha ao registrar face no serviço Python: " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
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



    public FaceSearchResponse buscarSuspeitosPorImagem(MultipartFile image, Integer topK) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            body.add("top_k", topK.toString());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FaceSearchResponse> response = restTemplate.postForEntity(
                    baseUrl + "/faces/search",
                    requestEntity,
                    FaceSearchResponse.class
            );

            return response.getBody();
        } catch (IOException e) {
            throw new InternalServerErrorException(
                    "Erro ao processar imagem: " + e.getMessage(),
                    e
            );
        } catch (RestClientException e) {
            throw new InternalServerErrorException(
                    "Falha ao buscar suspeitos no serviço Python: " + e.getMessage(),
                    e
            );
        }
    }

    public FaceSearchResponse buscarSuspeitosPorS3(String imageUrl, Integer topK) {
        String s3Path = toS3Path(imageUrl);

        FaceSearchRequest request = new FaceSearchRequest(topK, s3Path);

        try {
            ResponseEntity<FaceSearchResponse> response = restTemplate.postForEntity(
                    baseUrl + "/faces/search",
                    request,
                    FaceSearchResponse.class
            );

            return response.getBody();
        } catch (RestClientException e) {
            throw new InternalServerErrorException(
                    "Falha ao buscar suspeitos no serviço Python: " + e.getMessage(),
                    e
            );
        }
    }
}
