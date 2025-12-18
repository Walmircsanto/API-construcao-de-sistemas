package br.com.construcao.sistemas.service;


import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.request.suspect.UpdateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.AsyncSearchResponse;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.exception.ConflictException;
import br.com.construcao.sistemas.exception.InternalServerErrorException;
import br.com.construcao.sistemas.integration.dto.AsyncFaceSearchResponse;
import br.com.construcao.sistemas.integration.dto.FaceSearchRequest;
import br.com.construcao.sistemas.integration.dto.FaceSearchResponse;
import br.com.construcao.sistemas.integration.dto.suspect.ResponseSearchSuspect;
import br.com.construcao.sistemas.integration.service.PythonFaceService;
import br.com.construcao.sistemas.model.Image;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.enums.EnumProcessingStatus;
import br.com.construcao.sistemas.model.enums.OwnerType;
import br.com.construcao.sistemas.model.enums.SuspectStatus;
import br.com.construcao.sistemas.repository.ImageRepository;
import br.com.construcao.sistemas.repository.SuspectRepository;
import br.com.construcao.sistemas.util.helpers.AuthUserResolver;
import br.com.construcao.sistemas.model.User;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuspectService {

    private final SuspectRepository suspectRepository;
    private final ImageRepository imageRepository;
    private final MyModelMapper mapper;
    private final UploadFiles uploadFiles;
    private final PythonFaceService pythonFaceService;
    private final SearchResultService searchResultService;
    private final JobService jobService;
    private final AuthUserResolver authUserResolver;

    @Transactional
    public SuspectResponse create(CreateSuspectRequest req, @Nullable MultipartFile file) throws IOException {
        validarCpfDuplicado(req.getCpf());

        Suspect suspectData = mapper.mapTo(req, Suspect.class);
        suspectData = suspectRepository.save(suspectData);

        try {
            Image perfil = null;

            if (!file.isEmpty()) {
                perfil = salvarImagemDoSuspect(suspectData, file);

                String jobId = pythonFaceService.registrarFaceSuspeito(
                        suspectData.getId(),
                        perfil.getUrl(),
                        req
                );

                suspectData.setFaceProcessingJobId(jobId);
                suspectData.setFaceProcessingStatus(EnumProcessingStatus.PROCESSANDO);
                suspectRepository.save(suspectData);
            }

            return montarResponseComImagens(suspectData);
        } catch (Exception e) {
            throw new InternalServerErrorException("Erro ao criar suspect " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SuspectResponse get(Long id) {
        Suspect s = suspectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));
        return montarResponseComImagens(s);
    }

    @Transactional(readOnly = true)
    public Page<SuspectResponse> list(String query, SuspectStatus status, Pageable pageable) {
        return suspectRepository.findAllByFilters(query, status, pageable)
                .map(this::montarResponseComImagens);
    }

    @Transactional
    public SuspectResponse update(Long id, UpdateSuspectRequest req) {
        Suspect s = suspectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));

        if (req.getName() != null) s.setName(req.getName());
        if (req.getDescription() != null) s.setDescription(req.getDescription());
        if (req.getSuspectStatus() != null) s.setSuspectStatus(req.getSuspectStatus());

        if (req.getCpf() != null && !req.getCpf().equals(s.getCpf())) {
            validarCpfDuplicado(req.getCpf());
            s.setCpf(req.getCpf());
        }

        s = suspectRepository.save(s);
        return montarResponseComImagens(s);
    }


    @Transactional
    public void delete(Long id) {
        if (!suspectRepository.existsById(id)) throw new NotFoundException("Suspeito não encontrado");
        suspectRepository.deleteById(id);
    }

    @Transactional
    public ImageResponse addImage(Long suspectId, MultipartFile file) throws IOException {
        Suspect s = suspectRepository.findById(suspectId)
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));
        Image img = salvarImagemDoSuspect(s, file);
        return mapper.mapTo(img, ImageResponse.class);
    }

    @Transactional(readOnly = true)
    public List<ImageResponse> listImages(Long suspectId) {
        if (!suspectRepository.existsById(suspectId)) throw new NotFoundException("Suspeito não encontrado");
        return imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, suspectId)
                .stream().map(i -> mapper.mapTo(i, ImageResponse.class))
                .toList();
    }

    @Transactional
    public ResponseSearchSuspect buscarSuspeitosPorImagem(MultipartFile image, Integer topK) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("image not found");
        }
        String processed_url = uploadFiles.putObject(image);
        
        if(processed_url == null) throw new BadRequestException("Failed processing image");
        
        // Salva a imagem de busca no banco
        Image savedImage = salvarImagemDeBusca(image, processed_url);
        
        ResponseSearchSuspect response = this.pythonFaceService.buscarSuspeitosPorImagem(image, topK, processed_url);
        response.setImageId(savedImage.getId());
        
        return response;
    }

    public FaceSearchResponse buscarSuspeitosPorS3(FaceSearchRequest request) {
        return this.pythonFaceService.buscarSuspeitosPorS3(request.getS3Path(), request.getTopK());
    }

    public AsyncSearchResponse buscarSuspeitosPorS3Async(MultipartFile file) throws IOException {
        String suspectSearchUrl = uploadFiles.putObject(file);
        
        // Salva a imagem de busca no banco
        Image savedImage = salvarImagemDeBusca(file, suspectSearchUrl);
        
        String requestId = this.pythonFaceService.buscarSuspeitosPorS3Async(suspectSearchUrl,2);
        
        Long userId = br.com.construcao.sistemas.util.SecurityUtils.getCurrentUserId();
        searchResultService.createPendingSearch(requestId, userId);
        
        return new AsyncSearchResponse(requestId, savedImage.getId());
    }


    private void validarCpfDuplicado(String cpf) {
        if (suspectRepository.existsByCpf(cpf)) {
            throw new ConflictException("CPF já cadastrado");
        }
    }

    private Image salvarImagemDoSuspect(Suspect suspect, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) throw new BadRequestException("Arquivo de imagem ausente");

        String url = uploadFiles.putObject(file);
        if (url == null) throw new InternalServerErrorException("Falha ao salvar no bucket");

        Image img = Image.builder()
                .ownerType(OwnerType.SUSPECT)
                .suspect(suspect)
                .url(url)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();

        return imageRepository.save(img);
    }

    private Image salvarImagemDeBusca(MultipartFile file, String url) throws IOException {
        if (file == null || file.isEmpty()) throw new BadRequestException("Arquivo de imagem ausente");
        
        Image img = Image.builder()
                .ownerType(OwnerType.INCIDENT)
                .url(url)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();
        
        return imageRepository.save(img);
    }
    
    @Transactional
    public String createSearchJob(MultipartFile image, String location) throws IOException {
        User currentUser = authUserResolver.currentUser();
        return jobService.createSearchJob(image, location, currentUser);
    }

    private SuspectResponse montarResponseComImagens(Suspect s) {
        SuspectResponse resp = mapper.mapTo(s, SuspectResponse.class);
        List<Image> imgs = imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, s.getId());
        resp.setImages(imgs.stream().map(i -> mapper.mapTo(i, ImageResponse.class)).toList());
        return resp;
    }
}
