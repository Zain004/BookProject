package com.example.versjon2.Book.Valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {
    @Override
    public boolean isValid(LocalDate dob, ConstraintValidatorContext context) {
        return Optional.ofNullable(dob)
                .map(date -> Period.between(date, LocalDate.now()).getYears() >= 18)
                .orElse(false);
    }
}
