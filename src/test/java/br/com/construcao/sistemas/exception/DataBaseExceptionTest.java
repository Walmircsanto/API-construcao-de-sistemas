package br.com.construcao.sistemas.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataBaseExceptionTest {

    @Test
    void deveCriarExceptionComMensagem() {
        DataBaseException ex = new DataBaseException("Erro no banco");

        assertEquals("Erro no banco", ex.getMessage());
        assertNull(ex.getCause());
    }
}