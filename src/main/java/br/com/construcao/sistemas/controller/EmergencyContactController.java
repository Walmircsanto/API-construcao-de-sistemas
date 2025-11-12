package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.emergency.CreateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.request.emergency.UpdateEmergencyContactRequest;
import br.com.construcao.sistemas.controller.dto.response.emergency.EmergencyContactResponse;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.service.EmergencyContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/nexus/emergency-contacts")
@RequiredArgsConstructor
public class EmergencyContactController {

    private final EmergencyContactService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmergencyContactResponse> create(
            @RequestPart("data") CreateEmergencyContactRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        return ResponseEntity.ok(service.create(req, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmergencyContactResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(service.list(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyContactResponse> update(@PathVariable Long id, @RequestBody UpdateEmergencyContactRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/images", consumes = {"multipart/form-data"})
    public ResponseEntity<ImageResponse> addImage(@PathVariable Long id, @RequestPart("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(service.addImage(id, file));
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> listImages(@PathVariable Long id) {
        return ResponseEntity.ok(service.listImages(id));
    }
}
