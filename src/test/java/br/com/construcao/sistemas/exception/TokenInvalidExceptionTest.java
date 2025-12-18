package br.com.construcao.sistemas.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenInvalidExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Token inválido";

        TokenInvalidException exception = new TokenInvalidException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(true);
    }
}