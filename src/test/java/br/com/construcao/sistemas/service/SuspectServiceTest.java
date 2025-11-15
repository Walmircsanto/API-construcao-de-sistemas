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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SuspectServiceTest {

    private SuspectRepository suspectRepository;
    private ImageRepository imageRepository;
    private MyModelMapper mapper;
    private UploadFiles uploadFiles;

    private SuspectService service;

    @BeforeEach
    void setup() {
        suspectRepository = mock(SuspectRepository.class);
        imageRepository = mock(ImageRepository.class);
        mapper = mock(MyModelMapper.class);
        uploadFiles = mock(UploadFiles.class);

        service = new SuspectService(
                suspectRepository,
                imageRepository,
                mapper,
                uploadFiles
        );
    }

    @Test
    void testCreate_ComImagem() throws Exception {
        CreateSuspectRequest req = new CreateSuspectRequest();
        req.setCpf("123");
        req.setName("João");

        Suspect saved = new Suspect();
        saved.setId(1L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(100L);

        when(mapper.mapTo(req, Suspect.class)).thenReturn(new Suspect());
        when(suspectRepository.save(any())).thenReturn(saved);

        when(uploadFiles.putObject(file)).thenReturn("http://img.com/a.png");

        Image img = Image.builder().id(10L).url("http://img.com/a.png").build();
        when(imageRepository.save(any())).thenReturn(img);

        when(imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, 1L))
                .thenReturn(List.of(img));

        SuspectResponse respMapped = new SuspectResponse();
        when(mapper.mapTo(saved, SuspectResponse.class)).thenReturn(respMapped);
        when(mapper.mapTo(img, ImageResponse.class)).thenReturn(new ImageResponse());

        SuspectResponse resp = service.create(req, file);

        assertNotNull(resp);
        verify(suspectRepository).save(any());
        verify(uploadFiles).putObject(file);
    }

    @Test
    void testCreate_CpfDuplicado() {
        CreateSuspectRequest req = new CreateSuspectRequest();
        req.setCpf("111");

        when(suspectRepository.existsByCpf("111")).thenReturn(true);

        assertThrows(ConflictException.class, () ->
                service.create(req, null)
        );
    }

    @Test
    void testGet_Sucesso() {
        Suspect s = new Suspect();
        s.setId(1L);

        when(suspectRepository.findById(1L)).thenReturn(Optional.of(s));
        when(imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, 1L))
                .thenReturn(List.of());

        when(mapper.mapTo(s, SuspectResponse.class)).thenReturn(new SuspectResponse());

        SuspectResponse resp = service.get(1L);
        assertNotNull(resp);
    }

    @Test
    void testGet_NotFound() {
        when(suspectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.get(1L));
    }

    @Test
    void testList() {
        Suspect s = new Suspect();
        s.setId(1L);

        Page<Suspect> page = new PageImpl<>(List.of(s));
        when(suspectRepository.findAll(any(PageRequest.class))).thenReturn(page);

        when(imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, 1L))
                .thenReturn(List.of());

        when(mapper.mapTo(s, SuspectResponse.class)).thenReturn(new SuspectResponse());

        Page<SuspectResponse> resp = service.list(PageRequest.of(0, 10));

        assertEquals(1, resp.getTotalElements());
    }

    @Test
    void testUpdate_Sucesso() {
        Suspect s = new Suspect();
        s.setId(1L);
        s.setCpf("111");

        UpdateSuspectRequest req = new UpdateSuspectRequest();
        req.setName("Novo");
        req.setCpf("222");

        when(suspectRepository.findById(1L)).thenReturn(Optional.of(s));
        when(suspectRepository.existsByCpf("222")).thenReturn(false);

        when(suspectRepository.save(any())).thenReturn(s);

        when(mapper.mapTo(s, SuspectResponse.class)).thenReturn(new SuspectResponse());
        when(imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, 1L))
                .thenReturn(List.of());

        SuspectResponse resp = service.update(1L, req);

        assertNotNull(resp);
        assertEquals("222", s.getCpf());
        assertEquals("Novo", s.getName());
    }

    @Test
    void testUpdate_NotFound() {
        UpdateSuspectRequest req = new UpdateSuspectRequest();
        when(suspectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.update(1L, req));
    }

    @Test
    void testUpdate_CpfDuplicado() {
        Suspect s = new Suspect();
        s.setId(1L);
        s.setCpf("111");

        UpdateSuspectRequest req = new UpdateSuspectRequest();
        req.setCpf("222");

        when(suspectRepository.findById(1L)).thenReturn(Optional.of(s));
        when(suspectRepository.existsByCpf("222")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.update(1L, req));
    }

    @Test
    void testDelete_Sucesso() {
        when(suspectRepository.existsById(5L)).thenReturn(true);

        service.delete(5L);

        verify(suspectRepository).deleteById(5L);
    }

    @Test
    void testDelete_NotFound() {
        when(suspectRepository.existsById(5L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.delete(5L));
    }

    @Test
    void testAddImage_Sucesso() throws Exception {
        Suspect s = new Suspect();
        s.setId(1L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");
        when(file.getSize()).thenReturn(200L);

        when(suspectRepository.findById(1L)).thenReturn(Optional.of(s));
        when(uploadFiles.putObject(file)).thenReturn("http://url.com/img.png");

        Image savedImg = Image.builder().id(10L).build();
        when(imageRepository.save(any())).thenReturn(savedImg);

        ImageResponse mapped = new ImageResponse();
        when(mapper.mapTo(savedImg, ImageResponse.class)).thenReturn(mapped);

        ImageResponse resp = service.addImage(1L, file);

        assertNotNull(resp);
        verify(imageRepository).save(any());
    }

    @Test
    void testAddImage_SuspectNotFound() {
        when(suspectRepository.findById(1L)).thenReturn(Optional.empty());
        MultipartFile file = mock(MultipartFile.class);
        assertThrows(NotFoundException.class, () -> service.addImage(1L, file));
    }

    @Test
    void testListImages_Sucesso() {
        when(suspectRepository.existsById(1L)).thenReturn(true);

        Image img = Image.builder().id(10L).build();

        when(imageRepository.findByOwnerTypeAndSuspectId(OwnerType.SUSPECT, 1L))
                .thenReturn(List.of(img));

        when(mapper.mapTo(img, ImageResponse.class)).thenReturn(new ImageResponse());

        List<ImageResponse> list = service.listImages(1L);
        assertEquals(1, list.size());
    }

    @Test
    void testListImages_NotFound() {
        when(suspectRepository.existsById(1L)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> service.listImages(1L));
    }

    @Test
    void testSalvarImagemDoSuspect_ArquivoVazio() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThrows(NotFoundException.class, () ->
                service.addImage(1L, file)
        );
    }

    @Test
    void testSalvarImagemDoSuspect_FalhaBucket() throws Exception {
        Suspect s = new Suspect();
        s.setId(1L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        when(suspectRepository.findById(1L)).thenReturn(Optional.of(s));
        when(uploadFiles.putObject(file)).thenReturn(null);

        assertThrows(InternalServerErrorException.class, () ->
                service.addImage(1L, file)
        );
    }
}