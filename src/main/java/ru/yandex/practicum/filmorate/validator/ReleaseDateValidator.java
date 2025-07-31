package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.yandex.practicum.filmorate.interfaceAndAnnotation.ReleaseDateCheck;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDateCheck, LocalDate> {

    @Override
    public boolean isValid(LocalDate localDate, ConstraintValidatorContext constraintValidatorContext) {
        if (localDate == null) {
            return true;
        }

        return localDate.isAfter(LocalDate.of(1895, 12, 28));
    }
}
