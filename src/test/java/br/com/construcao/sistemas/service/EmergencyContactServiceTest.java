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
import br.com.construcao.sistemas.model.enums.ServiceType;
import br.com.construcao.sistemas.repository.EmergencyContactRepository;
import br.com.construcao.sistemas.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmergencyContactServiceTest {

    @Mock
    private EmergencyContactRepository emergencyContactRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private MyModelMapper mapper;

    @Mock
    private UploadFiles uploadFiles;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private EmergencyContactService service;

    private EmergencyContact ec;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        ec = EmergencyContact.builder()
                .id(1L)
                .name("Contato A")
                .phone("9999")
                .serviceType(ServiceType.PM)
                .build();
    }

    @Test
    void testDeveCriar_SemImagem() throws IOException {
        CreateEmergencyContactRequest req = new CreateEmergencyContactRequest();
        req.setName("Contato A");
        req.setPhone("9999");

        when(emergencyContactRepository.existsByPhone("9999")).thenReturn(false);
        when(mapper.mapTo(req, EmergencyContact.class)).thenReturn(ec);
        when(emergencyContactRepository.save(ec)).thenReturn(ec);
        when(imageRepository.findByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, 1L))
                .thenReturn(List.of());
        when(mapper.mapTo(ec, EmergencyContactResponse.class))
                .thenReturn(new EmergencyContactResponse());

        EmergencyContactResponse resp = service.create(req, null);

        assertNotNull(resp);
        verify(emergencyContactRepository).save(ec);
        verify(imageRepository).findByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, 1L);
    }

    @Test
    void testDeveLancarErro_QuandoTelefoneJaExiste() {
        CreateEmergencyContactRequest req = new CreateEmergencyContactRequest();
        req.setPhone("123");

        when(emergencyContactRepository.existsByPhone("123")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.create(req, null));
    }

    @Test
    void testDeveCriar_ComImagem() throws IOException {
        CreateEmergencyContactRequest req = new CreateEmergencyContactRequest();
        req.setPhone("9999");

        when(emergencyContactRepository.existsByPhone("9999")).thenReturn(false);
        when(mapper.mapTo(req, EmergencyContact.class)).thenReturn(ec);
        when(emergencyContactRepository.save(ec)).thenReturn(ec);

        when(file.isEmpty()).thenReturn(false);
        when(uploadFiles.putObject(file)).thenReturn("http://img.com/a.png");

        when(imageRepository.findByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, 1L))
                .thenReturn(List.of());
        when(mapper.mapTo(any(), eq(EmergencyContactResponse.class)))
                .thenReturn(new EmergencyContactResponse());

        service.create(req, file);

        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void testDeveLancarErro_QuandoUploadFalhar() throws IOException {
        CreateEmergencyContactRequest req = new CreateEmergencyContactRequest();
        req.setPhone("9999");

        when(emergencyContactRepository.existsByPhone("9999")).thenReturn(false);
        when(mapper.mapTo(req, EmergencyContact.class)).thenReturn(ec);
        when(emergencyContactRepository.save(ec)).thenReturn(ec);
        when(file.isEmpty()).thenReturn(false);
        when(uploadFiles.putObject(file)).thenReturn(null); // simula erro

        assertThrows(InternalServerErrorException.class,
                () -> service.create(req, file));
    }

    @Test
    void testDeveRetornarContato_QuandoExistir() {
        when(emergencyContactRepository.findById(1L)).thenReturn(Optional.of(ec));
        when(imageRepository.findByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, 1L))
                .thenReturn(List.of());
        when(mapper.mapTo(ec, EmergencyContactResponse.class))
                .thenReturn(new EmergencyContactResponse());

        EmergencyContactResponse resp = service.get(1L);

        assertNotNull(resp);
    }

    @Test
    void testDeveLancarNotFound_QuandoContatoNaoExistir() {
        when(emergencyContactRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get(1L));
    }

    @Test
    void testDeveListarContatos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmergencyContact> page = new PageImpl<>(List.of(ec));

        when(emergencyContactRepository.findAll(pageable)).thenReturn(page);
        when(mapper.mapTo(any(), eq(EmergencyContactResponse.class)))
                .thenReturn(new EmergencyContactResponse());
        when(imageRepository.findByOwnerTypeAndEmergencyContactId(any(), any()))
                .thenReturn(List.of());

        Page<EmergencyContactResponse> resp = service.list(pageable);

        assertEquals(1, resp.getTotalElements());
    }

    @Test
    void testDeveAtualizarCamposBasicos() throws IOException {
        UpdateEmergencyContactRequest req = new UpdateEmergencyContactRequest();
        req.setName("Novo Nome");

        when(emergencyContactRepository.findById(1L)).thenReturn(Optional.of(ec));
        when(emergencyContactRepository.save(ec)).thenReturn(ec);
        when(imageRepository.findByOwnerTypeAndEmergencyContactId(any(), any()))
                .thenReturn(List.of());
        when(mapper.mapTo(ec, EmergencyContactResponse.class))
                .thenReturn(new EmergencyContactResponse());

        EmergencyContactResponse resp = service.update(1L, req, null);

        assertNotNull(resp);
        assertEquals("Novo Nome", ec.getName());
    }

    @Test
    void testDeveLancarErro_QuandoNovoTelefoneJaExiste() {
        UpdateEmergencyContactRequest req = new UpdateEmergencyContactRequest();
        req.setPhone("8888");

        when(emergencyContactRepository.findById(1L)).thenReturn(Optional.of(ec));
        when(emergencyContactRepository.existsByPhone("8888")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> service.update(1L, req, null));
    }

    @Test
    void testDeveAtualizar_ComImagem() throws IOException {
        UpdateEmergencyContactRequest req = new UpdateEmergencyContactRequest();

        when(emergencyContactRepository.findById(1L)).thenReturn(Optional.of(ec));
        when(emergencyContactRepository.save(ec)).thenReturn(ec);

        when(file.isEmpty()).thenReturn(false);
        when(uploadFiles.putObject(file)).thenReturn("http://url.com/img");

        when(imageRepository.findByOwnerTypeAndEmergencyContactId(any(), any()))
                .thenReturn(List.of());
        when(mapper.mapTo(any(), eq(EmergencyContactResponse.class)))
                .thenReturn(new EmergencyContactResponse());

        service.update(1L, req, file);

        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void testDeveDeletarContatoEImagens() {
        when(emergencyContactRepository.findById(1L)).thenReturn(Optional.of(ec));

        service.delete(1L);

        verify(imageRepository)
                .deleteByOwnerTypeAndEmergencyContactId(OwnerType.EMERGENCY_CONTACT, 1L);

        verify(emergencyContactRepository).delete(ec);
    }

    @Test
    void testDeveLancarErro_QuandoDeletarInexistente() {
        when(emergencyContactRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.delete(1L));
    }

    @Test
    void testDeveListarImagens() {
        Image img = Image.builder().id(1L).url("url").build();

        when(emergencyContactRepository.existsById(1L)).thenReturn(true);
        when(imageRepository.findByOwnerTypeAndEmergencyContactId(any(), any()))
                .thenReturn(List.of(img));
        when(mapper.mapTo(any(), eq(ImageResponse.class)))
                .thenReturn(new ImageResponse());

        List<ImageResponse> list = service.listImages(1L);

        assertEquals(1, list.size());
    }

    @Test
    void testDeveLancarErro_QuandoListarImagensDeContatoInexistente() {
        when(emergencyContactRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> service.listImages(1L));
    }
}