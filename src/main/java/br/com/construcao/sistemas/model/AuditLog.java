package br.com.construcao.sistemas.model;

import br.com.construcao.sistemas.model.enums.AuditAction;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(nullable = false)
    private String entityType;

    private Long entityId;

    @Lob
    private String details;
}
