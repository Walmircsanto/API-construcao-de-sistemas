package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.request.SearchSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.JobResponse;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.service.JobService;
import br.com.construcao.sistemas.util.helpers.AuthUserResolver;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/nexus")
@RequiredArgsConstructor
public class SearchJobController {

    private final JobService jobService;
    private final AuthUserResolver authUserResolver;

    @PostMapping("/find-search-suspect")
    public ResponseEntity<String> findSearchSuspect(
            @RequestParam("image") MultipartFile image,
            @Valid @ModelAttribute SearchSuspectRequest request) throws IOException {
        
        User user = authUserResolver.currentUser();
        String jobId = jobService.createSearchJob(image, request.getLocation(), user);
        
        return ResponseEntity.ok(jobId);
    }

    @PostMapping("/web-hook")
    public ResponseEntity<Void> webHook(
            @RequestParam String imageWithBoundingBoxUrl,
            @RequestParam Long suspectId,
            @RequestParam String jobIdRequest) {
        
        jobService.completeJob(imageWithBoundingBoxUrl, suspectId, jobIdRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find-job-by-id/{jobIdRequest}")
    public ResponseEntity<JobResponse> findJobById(@PathVariable String jobIdRequest) {
        JobResponse job = jobService.findJobById(jobIdRequest);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/emit-alert-job/{jobIdRequest}")
    public ResponseEntity<Void> emitAlert(@PathVariable String jobIdRequest) {
        jobService.emitAlert(jobIdRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/simular-camera")
    public ResponseEntity<Long> simulateCamera() {
        Long incidentId = jobService.simulateCamera();
        return ResponseEntity.ok(incidentId);
    }
}