package br.com.construcao.sistemas.model;


import br.com.construcao.sistemas.model.enums.SuspectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_suspect")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suspect extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    private Integer age;

    @Column(nullable=false, unique = true, length = 14)
    private String cpf;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SuspectStatus suspectStatus = SuspectStatus.FORAGIDO;

    @OneToMany(mappedBy = "suspect", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Image> images = new ArrayList<>();
}
