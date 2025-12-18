package br.com.construcao.sistemas.util.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PasswordValidatorTest {

    private PasswordValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setup() {
        validator = new PasswordValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
        builder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void deveRetornarFalse_quandoSenhaForNull() {
        boolean result = validator.isValid(null, context);
        assertFalse(result);
    }

    @Test
    void deveRetornarFalse_quandoSenhaForVazia() {
        boolean result = validator.isValid("   ", context);
        assertFalse(result);
    }

    @Test
    void deveRetornarFalse_quandoSenhaTiverMenosQue8Caracteres() {
        boolean result = validator.isValid("Abc!12", context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("A senha deve ter no mínimo 8 caracteres");
    }

    @Test
    void deveRetornarFalse_quandoNaoTiverCaractereEspecial() {
        boolean result = validator.isValid("Senha1234", context);

        assertFalse(result);
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(
                "A senha deve conter pelo menos um caractere especial (!@#$%^&*()_+-=[]{}|;:',.<>?/`~)"
        );
    }

    @Test
    void deveRetornarTrue_quandoSenhaForValida() {
        boolean result = validator.isValid("Senha123!", context);
        assertTrue(result);

        verify(context, never()).disableDefaultConstraintViolation();
        verify(context, never()).buildConstraintViolationWithTemplate(anyString());
    }
}