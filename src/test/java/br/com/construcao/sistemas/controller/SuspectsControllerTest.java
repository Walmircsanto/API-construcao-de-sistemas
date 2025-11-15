package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.request.suspect.UpdateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.dto.response.page.PageResponse;
import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.service.SuspectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuspectsControllerTest {

    @Mock
    private SuspectService suspectService;

    @InjectMocks
    private SuspectsController controller;

    @Test
    void testCreate() throws IOException {
        CreateSuspectRequest req = new CreateSuspectRequest();
        SuspectResponse resp = new SuspectResponse();
        MultipartFile file = mock(MultipartFile.class);

        when(suspectService.create(req, file)).thenReturn(resp);

        ResponseEntity<SuspectResponse> response = controller.create(req, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(resp, response.getBody());
        verify(suspectService, times(1)).create(req, file);
    }

    @Test
    void testGet() {
        Long id = 1L;
        SuspectResponse resp = new SuspectResponse();
        when(suspectService.get(id)).thenReturn(resp);

        ResponseEntity<SuspectResponse> response = controller.get(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
        verify(suspectService, times(1)).get(id);
    }

    @Test
    void testList() {
        Page<SuspectResponse> page = new PageImpl<>(List.of(new SuspectResponse()));
        when(suspectService.list(PageRequest.of(0, 20))).thenReturn(page);

        ResponseEntity<PageResponse<SuspectResponse>> response = controller.list(0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page.getContent(), response.getBody().getItems());
        verify(suspectService, times(1)).list(PageRequest.of(0, 20));
    }

    @Test
    void testUpdate() {
        Long id = 1L;
        UpdateSuspectRequest req = new UpdateSuspectRequest();
        SuspectResponse resp = new SuspectResponse();
        when(suspectService.update(id, req)).thenReturn(resp);

        ResponseEntity<SuspectResponse> response = controller.update(id, req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
        verify(suspectService, times(1)).update(id, req);
    }

    @Test
    void testDelete() {
        Long id = 1L;

        ResponseEntity<Void> response = controller.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(suspectService, times(1)).delete(id);
    }

    @Test
    void testAddImage() throws IOException {
        Long id = 1L;
        MultipartFile file = mock(MultipartFile.class);
        ImageResponse imgResp = new ImageResponse();
        when(suspectService.addImage(id, file)).thenReturn(imgResp);

        ResponseEntity<ImageResponse> response = controller.addImage(id, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(imgResp, response.getBody());
        verify(suspectService, times(1)).addImage(id, file);
    }

    @Test
    void testListImages() {
        Long id = 1L;
        List<ImageResponse> imgs = List.of(new ImageResponse());
        when(suspectService.listImages(id)).thenReturn(imgs);

        ResponseEntity<List<ImageResponse>> response = controller.listImages(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(imgs, response.getBody());
        verify(suspectService, times(1)).listImages(id);
    }
}