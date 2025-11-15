package br.com.construcao.sistemas.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InternalServerErrorExceptionTest {
    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Erro interno no servidor";
        InternalServerErrorException exception = new InternalServerErrorException(errorMessage);

        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Erro grave";
        Throwable cause = new RuntimeException("Causa original");

        InternalServerErrorException exception = new InternalServerErrorException(errorMessage, cause);

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof RuntimeException);
    }
}