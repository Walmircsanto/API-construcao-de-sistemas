package br.com.construcao.sistemas.model;

import br.com.construcao.sistemas.model.enums.EnumStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    static class FakeEntity extends BaseEntity {}

    @Test
    void prePersist_deveDefinirDatasEStatusQuandoNulos() {
        FakeEntity entity = new FakeEntity();

        entity.onCreate();

        assertNotNull(entity.getDataCriacao());
        assertNotNull(entity.getDataAtualizacao());
        assertEquals(entity.getDataCriacao(), entity.getDataAtualizacao());
        assertEquals(EnumStatus.ATIVO, entity.getStatus());
    }

    @Test
    void prePersist_naoDeveSobrescreverStatusQuandoJaDefinido() {
        FakeEntity entity = new FakeEntity();
        entity.setStatus(EnumStatus.INATIVO);

        entity.onCreate();

        assertEquals(EnumStatus.INATIVO, entity.getStatus());
    }

    @Test
    void preUpdate_deveAtualizarDataAtualizacao() throws InterruptedException {
        FakeEntity entity = new FakeEntity();

        entity.onCreate();
        LocalDateTime oldUpdate = entity.getDataAtualizacao();

        Thread.sleep(5);

        entity.onUpdate();

        assertTrue(entity.getDataAtualizacao().isAfter(oldUpdate));
    }

    @Test
    void preUpdate_deveSetarStatusAtivoQuandoNulo() {
        FakeEntity entity = new FakeEntity();

        entity.onCreate();
        entity.setStatus(null);

        entity.onUpdate();

        assertEquals(EnumStatus.ATIVO, entity.getStatus());
    }

    @Test
    void preUpdate_naoDeveSobrescreverStatusQuandoJaDefinido() {
        FakeEntity entity = new FakeEntity();

        entity.onCreate();
        entity.setStatus(EnumStatus.INATIVO);

        entity.onUpdate();

        assertEquals(EnumStatus.INATIVO, entity.getStatus());
    }
}