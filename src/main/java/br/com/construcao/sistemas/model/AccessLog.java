package br.com.construcao.sistemas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tb_access_log")
@Getter
@Setter
@NoArgsConstructor
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String method;

    private String path;

    private Integer status;

    private String userEmail;

    private String ip;

    @Column(columnDefinition = "timestamp")
    private Instant timestamp = Instant.now();
}
