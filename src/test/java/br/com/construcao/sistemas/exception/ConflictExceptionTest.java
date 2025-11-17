package br.com.construcao.sistemas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class ConflictExceptionTest {


    @Test
    void testCriarExceptionComMensagem() {
        ConflictException ex = new ConflictException("Erro de conflito");

        assertEquals("Erro de conflito", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void testCriarExceptionComMensagemECausa() {
        Throwable cause = new RuntimeException("Causa original");
        ConflictException ex = new ConflictException("Erro de conflito", cause);

        assertEquals("Erro de conflito", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void testTerStatusHttpConflict() {
        ResponseStatus status = ConflictException.class.getAnnotation(ResponseStatus.class);

        assertNotNull(status);
        assertEquals(HttpStatus.CONFLICT, status.value());
    }
}