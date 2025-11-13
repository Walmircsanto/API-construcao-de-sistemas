package br.com.construcao.sistemas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "tb_password_reset_token", indexes = {
        @Index(name="idx_prt_user", columnList="user_id"),
        @Index(name="idx_prt_expires", columnList="expires_at")
})
@Getter
@Setter
public class PasswordResetToken extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Column(name="token_hash", nullable=false, length=64)
    private String tokenHash;

    @Column(name="expires_at", nullable=false)
    private Instant expiresAt;

    @Column(name="used", nullable=false)
    private boolean used = false;

    @Column(name="requested_ip", length=64)
    private String requestedIp;
}
