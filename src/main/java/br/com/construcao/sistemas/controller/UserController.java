package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.login.UpdatePasswordRequest;
import br.com.construcao.sistemas.controller.dto.request.login.UpdateUserRequest;
import br.com.construcao.sistemas.controller.dto.request.user.CreateUserRequest;
import br.com.construcao.sistemas.controller.dto.response.user.UserResponse;
import br.com.construcao.sistemas.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/nexus/user")
public class UserController {

    private final UserService service;

    public UserController(UserService userService) {
        this.service = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> create(
            @Valid @RequestPart("req") CreateUserRequest req,
            @RequestPart(name = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestPart("req") UpdateUserRequest req,
            @RequestPart(name = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(service.update(id, req, file));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                               @Valid @RequestBody UpdatePasswordRequest req) {
        service.updatePassword(id, req);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
