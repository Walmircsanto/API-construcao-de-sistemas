package br.com.construcao.sistemas.util.validators;

import br.com.construcao.sistemas.util.annotations.ValidPassword;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{}|;:',.<>?/`~";

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }

        // Verifica se tem no mínimo 8 caracteres
        if (password.length() < 8) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "A senha deve ter no mínimo 8 caracteres"
            ).addConstraintViolation();
            return false;
        }

        // Verifica se tem pelo menos um caractere especial
        boolean hasSpecialChar = password.chars()
                .anyMatch(ch -> SPECIAL_CHARACTERS.indexOf(ch) >= 0);

        if (!hasSpecialChar) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "A senha deve conter pelo menos um caractere especial (!@#$%^&*()_+-=[]{}|;:',.<>?/`~)"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
