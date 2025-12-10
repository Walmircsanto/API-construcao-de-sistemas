package br.com.construcao.sistemas.service;

import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.CompleteSearchRequest;
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

@Service
@RequiredArgsConstructor
public class SearchResultService {

    private final SearchResultRepository searchResultRepository;
    private final SuspectRepository suspectRepository;
    private final MyModelMapper mapper;

    @Transactional
    public void createPendingSearch(String requestId) {
        System.out.println("RequestId: " + requestId);
        SearchResult searchResult = SearchResult.builder()
                .requestId(requestId)
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
    }

    @Transactional
    public SearchResultResponse getSearchResult(String requestId) {
        System.out.println("Chego aqui com o RequestId: " + requestId);
        SearchResult searchResult = searchResultRepository.findByRequestId(requestId)
                .orElseThrow(() -> new NotFoundException("Search result not found"));

        System.out.println("Não chego aqui");
        SearchResultResponse response = new SearchResultResponse();
        response.setRequestId(searchResult.getRequestId());
        response.setStatus(searchResult.getStatus());
        
        if (searchResult.getStatus() == SearchStatus.COMPLETED && searchResult.getSuspectId() != null) {
            Suspect suspect = suspectRepository.findById(searchResult.getSuspectId())
                    .orElseThrow(() -> new NotFoundException("Suspect not found"));
            
            ResponseSearchSuspect suspectData = new ResponseSearchSuspect();
            suspectData.setSuspectId(suspect.getId());
            suspectData.setName(suspect.getName());
            suspectData.setBirthday(suspect.getBirthDate());
            suspectData.setStatus(suspect.getSuspectStatus());
            suspectData.setProcessedUrl(searchResult.getS3Path());
            suspectData.setDetectionLocation("Câmera 05");
            suspectData.setDetectionDate(LocalDate.now().toString());
            suspectData.setHorsDetection(LocalTime.now().toString());
            
            response.setSuspectData(suspectData);
        }
        
        return response;
    }
}