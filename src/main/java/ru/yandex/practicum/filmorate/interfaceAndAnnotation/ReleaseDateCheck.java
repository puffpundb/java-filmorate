package ru.yandex.practicum.filmorate.interfaceAndAnnotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validator.ReleaseDateValidator;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = ReleaseDateValidator.class)
public @interface ReleaseDateCheck {
    String message() default "Дата релиза не должна быть раньше 28.12.1895 г.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
