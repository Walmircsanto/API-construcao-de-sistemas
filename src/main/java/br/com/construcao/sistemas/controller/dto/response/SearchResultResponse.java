package br.com.construcao.sistemas.controller.dto.response;

import br.com.construcao.sistemas.integration.dto.suspect.ResponseSearchSuspect;
import br.com.construcao.sistemas.model.enums.SearchStatus;
import lombok.Data;

@Data
public class SearchResultResponse {
    private String requestId;
    private SearchStatus status;
    private ResponseSearchSuspect suspectData;
}