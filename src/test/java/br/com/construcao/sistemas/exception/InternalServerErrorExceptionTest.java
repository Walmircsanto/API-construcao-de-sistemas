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
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Erro grave";
        Throwable cause = new RuntimeException("Causa original");

        InternalServerErrorException exception = new InternalServerErrorException(errorMessage, cause);

        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertInstanceOf(RuntimeException.class, exception);
    }
}