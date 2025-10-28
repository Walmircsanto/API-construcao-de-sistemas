package br.com.construcao.sistemas.model;

import br.com.construcao.sistemas.model.enums.EnumStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Column(nullable = false, name = "dataatualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(nullable = false, name = "datacriacao")
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "TEXT default 'ATIVO'", nullable = false, name = "status")
    private EnumStatus status;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.dataCriacao = now;
        this.dataAtualizacao = now;
        if (this.status == null) this.status = EnumStatus.ATIVO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
        if (this.status == null) this.status = EnumStatus.ATIVO;
    }
}
