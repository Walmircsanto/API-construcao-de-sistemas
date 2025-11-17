package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.emergency.CreateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.request.emergency.UpdateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.response.emergency.EmergencyContactResponse;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.service.EmergencyContactService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmergencyContactControllerTest {

    @Mock
    private EmergencyContactService service;

    @InjectMocks
    private EmergencyContactController controller;

    @Test
    void testCreate() throws IOException {
        CreateEmergencyContactRequest req = new CreateEmergencyContactRequest();
        MultipartFile file = mock(MultipartFile.class);
        EmergencyContactResponse response = new EmergencyContactResponse();

        when(service.create(req, file)).thenReturn(response);

        ResponseEntity<EmergencyContactResponse> result = controller.create(req, file);

        assertEquals(response, result.getBody());
        verify(service, times(1)).create(req, file);
    }

    @Test
    void testGet() {
        Long id = 1L;
        EmergencyContactResponse response = new EmergencyContactResponse();

        when(service.get(id)).thenReturn(response);

        ResponseEntity<EmergencyContactResponse> result = controller.get(id);

        assertEquals(response, result.getBody());
        verify(service, times(1)).get(id);
    }

    @Test
    void testList() {
        Pageable pageable = Pageable.unpaged();
        List<EmergencyContactResponse> contacts = List.of(new EmergencyContactResponse());
        Page<EmergencyContactResponse> page = new PageImpl<>(contacts);

        when(service.list(pageable)).thenReturn(page);

        ResponseEntity<Page<EmergencyContactResponse>> result = controller.list(pageable);

        assertEquals(page, result.getBody());
        verify(service, times(1)).list(pageable);
    }

    @Test
    void testUpdate() throws IOException {
        Long id = 1L;
        UpdateEmergencyContactRequest req = new UpdateEmergencyContactRequest();
        MultipartFile file = mock(MultipartFile.class);
        EmergencyContactResponse response = new EmergencyContactResponse();

        when(service.update(id, req, file)).thenReturn(response);

        ResponseEntity<EmergencyContactResponse> result = controller.update(id, req, file);

        assertEquals(response, result.getBody());
        verify(service, times(1)).update(id, req, file);
    }

    @Test
    void testDelete() {
        Long id = 1L;

        ResponseEntity<Void> result = controller.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        verify(service, times(1)).delete(id);
    }

    @Test
    void testListImages() {
        Long id = 1L;
        List<ImageResponse> images = List.of(new ImageResponse());

        when(service.listImages(id)).thenReturn(images);

        ResponseEntity<List<ImageResponse>> result = controller.listImages(id);

        assertEquals(images, result.getBody());
        verify(service, times(1)).listImages(id);
    }
}