package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.login.UpdatePasswordRequest;
import br.com.construcao.sistemas.controller.dto.request.login.UpdateUserRequest;
import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.service.UserService;
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
class UserControllerTest {

    @Mock
    private UserService service;

    @InjectMocks
    private UserController controller;

    @Test
    void testCreate() throws IOException {
        CreateUserRequest req = new CreateUserRequest();
        MultipartFile file = mock(MultipartFile.class);
        UserResponse userResp = new UserResponse();

        when(service.create(req, file)).thenReturn(userResp);

        ResponseEntity<UserResponse> response = controller.create(req, file);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userResp, response.getBody());
        verify(service, times(1)).create(req, file);
    }

    @Test
    void testFindById() {
        Long id = 1L;
        UserResponse resp = new UserResponse();
        when(service.get(id)).thenReturn(resp);

        ResponseEntity<UserResponse> response = controller.findById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
        verify(service, times(1)).get(id);
    }

    @Test
    void testList() {
        Page<UserResponse> page = new PageImpl<>(List.of(new UserResponse()));
        when(service.list(PageRequest.of(0, 20))).thenReturn(page);

        ResponseEntity<Page<UserResponse>> response = controller.list(PageRequest.of(0, 20));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(page, response.getBody());
        verify(service, times(1)).list(PageRequest.of(0, 20));
    }

    @Test
    void testUpdate() throws IOException {
        Long id = 1L;
        UpdateUserRequest req = new UpdateUserRequest();
        MultipartFile file = mock(MultipartFile.class);
        UserResponse resp = new UserResponse();

        when(service.update(id, req, file)).thenReturn(resp);

        ResponseEntity<UserResponse> response = controller.update(id, req, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resp, response.getBody());
        verify(service, times(1)).update(id, req, file);
    }

    @Test
    void testUpdatePassword() {
        Long id = 1L;
        UpdatePasswordRequest req = new UpdatePasswordRequest();

        ResponseEntity<Void> response = controller.updatePassword(id, req);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service, times(1)).updatePassword(id, req);
    }

    @Test
    void testDelete() {
        Long id = 1L;

        ResponseEntity<Void> response = controller.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(service, times(1)).delete(id);
    }
}