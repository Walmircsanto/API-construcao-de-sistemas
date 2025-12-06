package br.com.construcao.sistemas.integration.service;

import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.exception.InternalServerErrorException;
import br.com.construcao.sistemas.integration.dto.*;
import br.com.construcao.sistemas.integration.dto.suspect.ResponseSearchSuspect;

import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.repository.SuspectRepository;
import br.com.construcao.sistemas.service.SuspectService;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PythonFaceService {

    private final RestTemplate restTemplate;
    private final SuspectRepository  suspectRepository;

    @Value("${nexus.python.base-url}")
    private String baseUrl;

    public String registrarFaceSuspeito(Long suspectId, String imageUrl, CreateSuspectRequest metadata) {
        String s3Path = toS3Path(imageUrl);
        FaceRegisterRequest body = new FaceRegisterRequest(
                suspectId,
                s3Path,
                metadata.getCpf()
        );

        try {
            ResponseEntity<FaceRegisterResponse> response = restTemplate.postForEntity(
                    baseUrl + "/faces/register",
                    body,
                    FaceRegisterResponse.class
            );

            return response.getBody().getJobId();

        } catch (RestClientException e) {
            e.printStackTrace();
            throw new InternalServerErrorException(
                    "Falha ao registrar face no serviço Python: " + e.getMessage(),
                    e
            );
        }
    }

//    public void registrarSuspeitoImagem(SuspectData suspectData, MultipartFile image, String s3Path){
//
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//            body.add("suspect_id", suspectData.getSuspectId().toString());
//            body.add("s3_path", s3Path);
//            body.add("cpf", suspectData.getCpfSuspect());
//
//            // Arquivo: MultipartFile -> ByteArrayResource
//            ByteArrayResource imageRequest = new ByteArrayResource(image.getBytes()) {
//                @Override
//                public String getFilename() {
//                    return image.getOriginalFilename();
//                }
//            };
//            body.add("image", imageRequest);
//
//            System.out.println("Enviando requisição para API Python...");
//            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
//
//            ResponseEntity<Void> response = restTemplate.postForEntity(
//                    baseUrl + "/faces/register",
//                    request,
//                    Void.class
//            );
//
//            System.out.println("Resposta da API Python: " + response.getStatusCode());
//
//        } catch (RestClientException e) {
//            System.err.println("Erro de comunicação com API Python: " + e.getMessage());
//            e.printStackTrace();
//            throw new InternalServerErrorException(
//                    "Falha ao registrar face no serviço Python: " + e.getMessage());
//        } catch (IOException e) {
//            System.err.println("Erro ao processar arquivo: " + e.getMessage());
//            throw new InternalServerErrorException("Erro ao processar arquivo de imagem: " + e.getMessage());
//        }
//    }

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



    public ResponseSearchSuspect buscarSuspeitosPorImagem(MultipartFile image, Integer topK) {
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

            String metadata = response.getBody().getMatches().get(0).getMetadata();
            return responseSearchSuspect(metadata);
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

    private ResponseSearchSuspect responseSearchSuspect(String metadata){
     String documento = metadata.split(":")[1];
        Suspect suspect = this.suspectRepository.findByCpf(metadata).get();

        ResponseSearchSuspect response = new ResponseSearchSuspect();

         response.setBirthday(suspect.getBirthDate());
         response.setName(suspect.getName());
         response.setStatus(suspect.getSuspectStatus());
         response.setHours(LocalTime.now().toString());

         return response;

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
