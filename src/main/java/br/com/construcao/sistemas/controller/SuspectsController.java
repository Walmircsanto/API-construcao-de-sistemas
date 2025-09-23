package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.request.suspect.UpdateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.page.PageResponse;
import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.service.SuspectService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/nexus/suspects")
public class SuspectsController {

    private final SuspectService suspectService;

    public SuspectsController(SuspectService suspectService) {
        this.suspectService = suspectService;
    }

    @PostMapping
    public ResponseEntity<SuspectResponse> create(@Valid @RequestBody CreateSuspectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(suspectService.create(req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuspectResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(suspectService.get(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<SuspectResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<SuspectResponse> p = suspectService.list(PageRequest.of(page, Math.min(size, 100)));
        return ResponseEntity.ok(PageResponse.of(p));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuspectResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateSuspectRequest req) {
        return ResponseEntity.ok(suspectService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        suspectService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
