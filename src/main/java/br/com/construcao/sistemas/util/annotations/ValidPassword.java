package br.com.construcao.sistemas.util.annotations;

import br.com.construcao.sistemas.util.validators.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "A senha deve ter no mínimo 8 caracteres e conter pelo menos um caractere especial";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
