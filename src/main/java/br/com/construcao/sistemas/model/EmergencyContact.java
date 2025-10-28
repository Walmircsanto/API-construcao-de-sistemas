package br.com.construcao.sistemas.model;

import br.com.construcao.sistemas.model.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_emergency_contact")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyContact extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(nullable=false, length = 32)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceType serviceType;
}
