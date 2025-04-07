package com.example.versjon2.ExamUserTask.Valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;
// Mal for å definere en egen annotasjon i JAVA
@Documented // Angir at annotasjonen skal inkluderes i Javadoc
@Constraint(validatedBy = AgeValidator.class)  // Spesifiserer at AgeValidator skal brukes for validering
@Target(ElementType.FIELD)  // Angir at annotasjonen kan brukes på felter
@Retention(RetentionPolicy.RUNTIME) // Angir at annotasjonen skal være tilgjengelig i kjøretiden
public @interface ValidAge {
    // Standard melding som vises når validering feiler
    String message() default "You must be at least 18 years old.";  // Standard feilmelding
    // Grupper som kan brukes for å spesifisere hvilke valideringsgrupper denne annotasjonen tilhører
    Class<?>[] groups() default {};
    // Payload er brukt til å overføre metadata om validering
    Class<? extends Payload>[] payload() default {};
}
