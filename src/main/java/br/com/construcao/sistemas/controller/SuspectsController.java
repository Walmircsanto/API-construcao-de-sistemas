package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.request.suspect.UpdateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.image.ImageResponse;
import br.com.construcao.sistemas.controller.dto.response.page.PageResponse;
import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.service.SuspectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/nexus/suspects")
public class SuspectsController {

    private final SuspectService suspectService;

    public SuspectsController(SuspectService suspectService) {
        this.suspectService = suspectService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SuspectResponse> create(
            @Valid @RequestPart("data") CreateSuspectRequest req,
            @RequestPart(name = "file", required = false) MultipartFile file
    ) throws IOException {
        SuspectResponse body = suspectService.create(req, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuspectResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(suspectService.get(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SuspectResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<SuspectResponse> p = suspectService.list(PageRequest.of(page, Math.min(size, 100)));
        return ResponseEntity.ok(PageResponse.of(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuspectResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSuspectRequest req
    ) {
        return ResponseEntity.ok(suspectService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        suspectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponse> addImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        ImageResponse img = suspectService.addImage(id, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(img);
    }

    @GetMapping("/{id}/images")
    public ResponseEntity<List<ImageResponse>> listImages(@PathVariable Long id) {
        return ResponseEntity.ok(suspectService.listImages(id));
    }
}
