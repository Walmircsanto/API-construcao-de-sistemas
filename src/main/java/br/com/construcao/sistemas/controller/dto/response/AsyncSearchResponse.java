package br.com.construcao.sistemas.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncSearchResponse {
    private String requestId;
    private Long imageId;
}