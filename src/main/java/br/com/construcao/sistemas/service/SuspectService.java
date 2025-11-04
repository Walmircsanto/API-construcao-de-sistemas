package br.com.construcao.sistemas.service;


import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.request.suspect.UpdateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.controller.exceptions.ConflictException;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.exception.InternalServerErrorException;
import br.com.construcao.sistemas.model.Image;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.enums.OwnerType;
import br.com.construcao.sistemas.repository.ImageRepository;
import br.com.construcao.sistemas.repository.SuspectRepository;
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

    @Transactional
    public SuspectResponse create(CreateSuspectRequest req, @Nullable MultipartFile file) throws IOException {
        validarCpfDuplicado(req.getCpf());

        Suspect s = mapper.mapTo(req, Suspect.class);
        s = suspectRepository.save(s);

        if (file != null && !file.isEmpty()) {
            salvarImagemDoSuspect(s, file);
        }

        return montarResponseComImagens(s);
    }

    @Transactional(readOnly = true)
    public SuspectResponse get(Long id){
        Suspect s = suspectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));
        return montarResponseComImagens(s);
    }

    @Transactional(readOnly = true)
    public Page<SuspectResponse> list(Pageable pageable){
        return suspectRepository.findAll(pageable)
                .map(this::montarResponseComImagens);
    }

    @Transactional
    public SuspectResponse update(Long id, UpdateSuspectRequest req){
        Suspect s = suspectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));

        if (req.getName() != null) s.setName(req.getName());
        if (req.getAge() != null)  s.setAge(req.getAge());
        if (req.getDescription() != null) s.setDescription(req.getDescription());

        if (req.getCpf() != null && !req.getCpf().equals(s.getCpf())) {
            validarCpfDuplicado(req.getCpf());
            s.setCpf(req.getCpf());
        }

        s = suspectRepository.save(s);
        return montarResponseComImagens(s);
    }

    @Transactional
    public void delete(Long id){
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
    public List<ImageResponse> listImages(Long suspectId){
        if (!suspectRepository.existsById(suspectId)) throw new NotFoundException("Suspeito não encontrado");
        return imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, suspectId)
                .stream().map(i -> mapper.mapTo(i, ImageResponse.class))
                .toList();
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

    private SuspectResponse montarResponseComImagens(Suspect s) {
        SuspectResponse resp = mapper.mapTo(s, SuspectResponse.class);
        List<Image> imgs = imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, s.getId());
        resp.setImages(imgs.stream().map(i -> mapper.mapTo(i, ImageResponse.class)).toList());
        return resp;
    }
}
