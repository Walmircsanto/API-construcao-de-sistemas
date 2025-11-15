package br.com.construcao.sistemas.exception;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionResponseTest {

    @Test
    void testCriarExceptionResponseComValoresCorretos() {
        Date now = new Date();
        String message = "Erro ocorrido";
        String details = "Detalhes do erro";

        ExceptionResponse response = new ExceptionResponse(now, message, details);

        assertEquals(now, response.getTimestamp());
        assertEquals(message, response.getMessage());
        assertEquals(details, response.getDetails());
    }

    @Test
    void testgettersNaoDevemSerNulosQuandoValoresForemPassados() {
        ExceptionResponse response = new ExceptionResponse(new Date(), "msg", "detalhes");

        assertNotNull(response.getTimestamp());
        assertNotNull(response.getMessage());
        assertNotNull(response.getDetails());
    }
}