package br.com.construcao.sistemas.controller.dto.response.acesslog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccessLogReportDTO {
    private Long id;
    private String method;
    private String path;
    private Integer statusCode;
    private String userEmail;
    private String ip;
    private String userAgent;
    private Long responseTimeMs;
    private LocalDateTime createdAt;
}
