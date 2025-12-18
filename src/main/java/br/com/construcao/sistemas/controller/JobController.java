package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.controller.dto.response.JobResponse;
import br.com.construcao.sistemas.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nexus/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping("/{jobIdRequest}")
    public ResponseEntity<JobResponse> findJobById(@PathVariable String jobIdRequest) {
        JobResponse job = jobService.findJobById(jobIdRequest);
        return ResponseEntity.ok(job);
    }

    @PostMapping("/{jobIdRequest}/emit-alert")
    public ResponseEntity<Void> emitAlert(@PathVariable String jobIdRequest) {
        jobService.emitAlert(jobIdRequest);
        return ResponseEntity.ok().build();
    }

}