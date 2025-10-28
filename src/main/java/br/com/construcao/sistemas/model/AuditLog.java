package br.com.construcao.sistemas.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_access_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;
    private String path;
    private Integer statusCode;
    private String userEmail;
    private String ip;
    private String userAgent;
    private Long responseTimeMs;
    private String requestId;
}
