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

    @Column(nullable = false)
    private Long suspectId;

    @Column(nullable = false, length = 512)
    private String url;

    private String contentType;
    private Long sizeBytes;
}
