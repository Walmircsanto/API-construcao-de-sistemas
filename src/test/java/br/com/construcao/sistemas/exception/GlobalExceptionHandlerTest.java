package br.com.construcao.sistemas.exception;

import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleUnauthorized() {
        UnauthorizedException ex = new UnauthorizedException("Unauthorized error");
        ResponseEntity<ExceptionResponse> response = handler.handleUnauthorized(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Unauthorized error", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("Unauthorized", response.getBody().getDetails());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleInternalServerError() {
        InternalServerErrorException ex = new InternalServerErrorException("Server failure");
        ResponseEntity<ExceptionResponse> response = handler.handleInternalServerError(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Server failure", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("Internal server error", response.getBody().getDetails());
    }

    @Test
    void testHandleConflict() {
        ConflictException ex = new ConflictException("Conflict happened");
        ResponseEntity<ExceptionResponse> response = handler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict happened", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("Conflict", response.getBody().getDetails());
    }

    @Test
    void testHandleDataBaseException() {
        DataBaseException ex = new DataBaseException("DB crash");
        ResponseEntity<ExceptionResponse> response = handler.handleDataBaseException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("DB crash", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("Database error", response.getBody().getDetails());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<ExceptionResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Something went wrong. Please try again later.", response.getBody().getMessage());
        assertEquals("Generic error", response.getBody().getDetails());
    }

    @Test
    void testHandleClientAbortException() {
        ClientAbortException ex = new ClientAbortException("Client aborted");
        ResponseEntity<String> response = handler.handleClientAbortException(ex);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Conexão abortada pelo cliente.", response.getBody());
    }

    @Test
    void testHandleTokenExpiredException() {
        TokenExpiredException ex = new TokenExpiredException("Expired token");
        ResponseEntity<ExceptionResponse> response = handler.handleTokenExpiredException(ex);

        assertEquals(HttpStatus.GONE, response.getStatusCode());
        assertEquals("Token expired.", Objects.requireNonNull(response.getBody()).getMessage());
        assertEquals("Expired token", response.getBody().getDetails());
    }

    @Test
    void testHandleRuntime() {
        RuntimeException ex = new RuntimeException("Runtime err");
        ResponseEntity<String> response = handler.handleRuntime(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Não foi possível completar o cadastro.", response.getBody());
    }
}