package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.emergency.CreateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.request.emergency.UpdateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.response.emergency.EmergencyContactResponse;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.exceptions.BadRequestException;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.exception.InternalServerErrorException;
import br.com.construcao.sistemas.model.EmergencyContact;
import br.com.construcao.sistemas.model.Image;
import br.com.construcao.sistemas.model.enums.OwnerType;
import br.com.construcao.sistemas.repository.EmergencyContactRepository;
import br.com.construcao.sistemas.repository.ImageRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmergencyContactService {

    private final EmergencyContactRepository emergencyContactRepository;
    private final ImageRepository imageRepository;
    private final MyModelMapper mapper;
    private final UploadFiles uploadFiles;

    @Transactional
    public EmergencyContactResponse create(CreateEmergencyContactRequest req,
                                           @Nullable MultipartFile file) throws IOException {

        if (req.getPhone() != null && emergencyContactRepository.existsByPhone(req.getPhone())) {
            throw new BadRequestException("Telefone já cadastrado");
        }

        EmergencyContact ec = mapper.mapTo(req, EmergencyContact.class);
        ec = emergencyContactRepository.save(ec);

        if (file != null && !file.isEmpty()) {
            imageRepository.deleteByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, ec.getId());
            salvarImagemDoEmergencyContact(ec, file);
        }

        return montarResponseComImagens(ec);
    }

    @Transactional(readOnly = true)
    public EmergencyContactResponse get(Long id) {
        EmergencyContact ec = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contato de emergência não encontrado"));
        return montarResponseComImagens(ec);
    }

    @Transactional(readOnly = true)
    public Page<EmergencyContactResponse> list(Pageable pageable) {
        return emergencyContactRepository.findAll(pageable)
                .map(this::montarResponseComImagens);
    }

    @Transactional
    public EmergencyContactResponse update(Long id,
                                           UpdateEmergencyContactRequest req,
                                           @Nullable MultipartFile file) throws IOException {

        EmergencyContact ec = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contato de emergência não encontrado"));

        if (req.getName() != null) {
            ec.setName(req.getName());
        }

        if (req.getPhone() != null && !req.getPhone().equals(ec.getPhone())) {
            if (emergencyContactRepository.existsByPhone(req.getPhone())) {
                throw new BadRequestException("Telefone já cadastrado");
            }
            ec.setPhone(req.getPhone());
        }

        if (req.getServiceType() != null) {
            ec.setServiceType(req.getServiceType());
        }

        ec = emergencyContactRepository.save(ec);

        if (file != null && !file.isEmpty()) {
            imageRepository.deleteByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, ec.getId());
            salvarImagemDoEmergencyContact(ec, file);
        }

        return montarResponseComImagens(ec);
    }

    @Transactional
    public void delete(Long id) {
        EmergencyContact ec = emergencyContactRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contato de emergência não encontrado"));

        imageRepository.deleteByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, id);
        emergencyContactRepository.delete(ec);
    }

    @Transactional(readOnly = true)
    public List<ImageResponse> listImages(Long emergencyContactId) {
        if (!emergencyContactRepository.existsById(emergencyContactId)) {
            throw new NotFoundException("Contato de emergência não encontrado");
        }

        return imageRepository.findByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, emergencyContactId)
                .stream()
                .map(i -> mapper.mapTo(i, ImageResponse.class))
                .toList();
    }

    private void salvarImagemDoEmergencyContact(EmergencyContact ec, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Arquivo de imagem ausente");
        }

        String url = uploadFiles.putObject(file);
        if (url == null) {
            throw new InternalServerErrorException("Falha ao salvar no bucket");
        }

        Image img = Image.builder()
                .ownerType(OwnerType.EMERGENCY_CONTACT)
                .emergencyContact(ec)
                .url(url)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .build();

        imageRepository.save(img);
    }

    private EmergencyContactResponse montarResponseComImagens(EmergencyContact ec) {
        EmergencyContactResponse resp = mapper.mapTo(ec, EmergencyContactResponse.class);
        List<Image> imgs = imageRepository.findByOwnerTypeAndEmergencyContactId(
                OwnerType.EMERGENCY_CONTACT, ec.getId()
        );
        resp.setImages(imgs.stream()
                .map(i -> mapper.mapTo(i, ImageResponse.class))
                .toList());
        return resp;
    }
}
