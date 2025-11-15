package br.com.construcao.sistemas.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenExpiredExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Token expirado";

        TokenExpiredException exception = new TokenExpiredException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(true);
    }
}