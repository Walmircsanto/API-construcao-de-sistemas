package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.CompleteSearchRequest;
import br.com.construcao.sistemas.controller.dto.request.notification.NotificationRequest;
import br.com.construcao.sistemas.controller.dto.response.SearchResultResponse;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.integration.dto.suspect.ResponseSearchSuspect;
import br.com.construcao.sistemas.model.SearchResult;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.enums.SearchStatus;
import br.com.construcao.sistemas.repository.SearchResultRepository;
import br.com.construcao.sistemas.repository.SuspectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchResultService {

    private final SearchResultRepository searchResultRepository;
    private final SuspectRepository suspectRepository;
    private final MyModelMapper mapper;
    private final NotificationProducer notificationProducer;

    @Transactional
    public void createPendingSearch(String requestId, Long userId) {
        System.out.println("RequestId: " + requestId);
        SearchResult searchResult = SearchResult.builder()
                .requestId(requestId)
                .userId(userId)
                .status(SearchStatus.PENDING)
                .build();
        searchResultRepository.save(searchResult);
    }

    @Transactional
    public void completeSearch(CompleteSearchRequest request) {
        SearchResult searchResult = searchResultRepository.findByRequestId(request.getRequestId())
                .orElseThrow(() -> new NotFoundException("Search result not found"));

        searchResult.setStatus(SearchStatus.COMPLETED);
        searchResult.setSuspectId(request.getIdSuspect());
        searchResult.setS3Path(request.getS3Path());
        searchResult.setCompletedAt(LocalDateTime.now());
        
        searchResultRepository.save(searchResult);
        
        // Notifica apenas o usuário que fez a requisição
        enviarNotificacaoParaUsuario(searchResult, request.getIdSuspect());
    }


    
    private void enviarNotificacaoParaUsuario(SearchResult searchResult, Long suspectId) {
        if (searchResult.getUserId() == null) {
            return;
        }
        
        NotificationRequest notification = new NotificationRequest();
        notification.setTitle("Busca Concluída");
        
        if (suspectId != null) {
            notification.setBody("Suspeito encontrado na sua busca por imagem!");
        } else {
            notification.setBody("Busca por imagem concluída. Nenhum suspeito encontrado.");
        }
        
        notification.setTarget("INCIDENT");
        notification.setId(searchResult.getRequestId()); // usar esse ID para buscar esse JOB
                                                         // GET JOB
        notification.setAction("view_result");
        notification.setUserIds(List.of(searchResult.getUserId()));
        
        notificationProducer.enqueueToUsers(notification);
    }
}