package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.response.JobResponse;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.integration.service.PythonFaceService;
import br.com.construcao.sistemas.model.Incident;
import br.com.construcao.sistemas.model.Job;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.model.enums.IncidentStatus;
import br.com.construcao.sistemas.model.enums.JobStatus;
import br.com.construcao.sistemas.repository.JobRepository;
import br.com.construcao.sistemas.repository.SuspectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final SuspectRepository suspectRepository;
    private final UploadFiles uploadFiles;
    private final PushNotificationService pushNotificationService;
    private final IncidentService incidentService;
    private final PythonFaceService pythonFaceService;

    @Transactional
    public String createSearchJob(MultipartFile image, String location, User user) throws IOException {
        String imageUrl = uploadFiles.putObject(image);

        Incident incident = Incident.builder()
                .location(location)
                .imageUrl(imageUrl)
                .incidentStatus(IncidentStatus.EM_PROCESSAMENTO)
                .assignedUser(user)
                .build();

        String jobIdRequest = this.pythonFaceService.buscarSuspeitosPorS3Async(imageUrl,1);
        Job job = Job.builder()
                .userRequest(user)
                .incident(incident)
                .status(JobStatus.PROCESSANDO)
                .jobIdRequest(jobIdRequest)
                .build();

        jobRepository.save(job);


        return jobIdRequest;
    }

    @Transactional
    public void completeJob(String imageWithBoundingBoxUrl, Long suspectId, String jobIdRequest) {
        Job job = jobRepository.findByJobIdRequest(jobIdRequest)
                .orElseThrow(() -> new NotFoundException("Job não encontrado"));

        Suspect suspect = suspectRepository.findById(suspectId)
                .orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));

        job.getIncident().setImageWithBoundingBoxUrl(imageWithBoundingBoxUrl);
        job.getIncident().setSuspect(suspect);
        job.setStatus(JobStatus.FINALIZADO);

        jobRepository.save(job);

        pushNotificationService.sendNotificationToUser(
                job.getUserRequest().getFcmToken(),
                "Busca Concluída",
                "Suspeito encontrado na sua busca",
                "JOB_COMPLETED",
                jobIdRequest
        );
    }

    public JobResponse findJobById(String jobIdRequest) {
        Job job = jobRepository.findByJobIdRequest(jobIdRequest)
                .orElseThrow(() -> new NotFoundException("Job não encontrado"));

        return JobResponse.builder()
                .id(job.getId())
                .jobIdRequest(job.getJobIdRequest())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    @Transactional
    public void emitAlert(String jobIdRequest) {
        Job job = jobRepository.findByJobIdRequest(jobIdRequest)
                .orElseThrow(() -> new NotFoundException("Job não encontrado"));

        Incident savedIncident = incidentService.saveIncident(job.getIncident());

        pushNotificationService.sendNotificationToAll(
                "Novo Incidente",
                "Um novo incidente foi registrado",
                "INCIDENT",
                job.getIncident().getImageUrl(),
                savedIncident.getId().toString()
        );
    }

    @Transactional
    public Long simulateCamera(MultipartFile imageWithoutBoundingBox, MultipartFile imageWithBoundingBox, Long suspectId) throws IOException {
        String imageUrl = uploadFiles.putObject(imageWithoutBoundingBox);
        String s3Path = uploadFiles.putObject(imageWithBoundingBox);
        
        Incident incident = incidentService.createMockIncidentWithImages(imageUrl, s3Path,suspectId);

        pushNotificationService.sendNotificationToAll(
                "Câmera Detectou Suspeito",
                "Um suspeito foi detectado pela câmera",
                "INCIDENT",
                imageUrl,
                incident.getId().toString()
        );

        return incident.getId();
    }
}