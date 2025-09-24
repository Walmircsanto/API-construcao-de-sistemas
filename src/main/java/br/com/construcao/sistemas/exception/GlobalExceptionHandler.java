package br.com.construcao.sistemas.exception;

import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorized(UnauthorizedException ex) {
        ExceptionResponse response = new ExceptionResponse(new Date(), ex.getMessage(), "Unauthorized");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InternalServerErrorException.class)
    public ResponseEntity<ExceptionResponse> handleInternalServerError(InternalServerErrorException ex) {
        ExceptionResponse response = new ExceptionResponse(new Date(), ex.getMessage(), "Internal server error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ExceptionResponse> handleConflict(ConflictException ex) {
        ExceptionResponse response = new ExceptionResponse(new Date(), ex.getMessage(), "Conflict");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
    @ExceptionHandler(DataBaseException.class)
    public ResponseEntity<ExceptionResponse> handleDataBaseException(DataBaseException ex) {
        ExceptionResponse response = new ExceptionResponse(new Date(), ex.getMessage(), "Database error");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        ExceptionResponse response = new ExceptionResponse(new Date(), "Something went wrong. Please try again later.", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(ClientAbortException.class)
    public ResponseEntity<String> handleClientAbortException(ClientAbortException ex) {

        return ResponseEntity.status(HttpStatus.OK).body("Conexão abortada pelo cliente.");
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ExceptionResponse> handleTokenExpiredException(Exception ex) {
        ExceptionResponse response = new ExceptionResponse(new Date(), "Token expired.",ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.GONE);
    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Não foi possível completar o cadastro.");
    }



}
