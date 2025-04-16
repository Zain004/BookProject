package com.example.versjon2.ExamUserTask.Entity;

import com.example.versjon2.ExamUserTask.Valid.ValidAge;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Email;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Builder
public class UsersDB {
    private Long id;

    @NotBlank(message="Firstname cannot be blank.")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)$",
            message = "Firstname name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message="Lastname cannot be blank.")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)$",
            message = "Lastname name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Lastname must be between 2 and 50 characters")
    private String lastName;

    @ValidAge
    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Pattern(regexp = "^[0-9]{8}$", message = "Phone must be 8 digits.")
    private String phone;

    @Email(message = "Please enter a valid email address.")
    private String email;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public UsersDB(String firstName, String lastName, LocalDate dob, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.phone = phone;
        this.email = email;
    }
}
