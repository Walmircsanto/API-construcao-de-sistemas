package br.com.construcao.sistemas.model;

import br.com.construcao.sistemas.model.enums.OwnerType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnerType ownerType;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "suspect_id")
    private Suspect suspect;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "emergency_contact_id")
    private EmergencyContact emergencyContact;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "incident_id")
    private Incident incident;

    @Column(nullable = false, length = 512)
    private String url;

    private String contentType;
    private Long sizeBytes;
}
