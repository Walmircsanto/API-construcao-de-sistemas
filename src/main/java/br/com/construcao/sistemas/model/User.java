package br.com.construcao.sistemas.model;

import br.com.construcao.sistemas.model.enums.AuthProvider;
import br.com.construcao.sistemas.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Table(name="tb_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(unique=true, nullable=false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable=false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider = AuthProvider.LOCAL;

    @Column(length=64)
    private String googleSub;

    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @Column(name = "fcm_token_updated_at")
    private Instant fcmTokenUpdatedAt;

    private boolean enabled = true;
    private boolean locked = false;

    private Integer failedLogins = 0;
    private Instant lastFailureAt;
    private Instant lastLoginAt;
}
