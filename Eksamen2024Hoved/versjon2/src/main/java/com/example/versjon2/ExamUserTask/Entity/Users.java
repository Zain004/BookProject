package com.example.versjon2.ExamUserTask.Entity;

import com.example.versjon2.Book.Valid.ValidAge;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.CodePointLength;
import org.hibernate.validator.constraints.Email;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(force = true) // uten noargsconstructor vil ikke jpa kunne lage et objekt
@AllArgsConstructor() // kan brukes sammen med builder
@Table (name = "users",
        indexes = {
            @Index(name = "idx_firstname", columnList = "first_name"), // Indeksen idx_firstname blir lagt til på kolonnen firstname.
            @Index(name = "idx_lastname", columnList = "last_name"),
            @Index(name = "idx_phone", columnList = "phone")
        },
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"email"}),
            @UniqueConstraint(columnNames = {"phone"})
        }
)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @NotBlank(message="Firstname cannot be blank.")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)$",
            message = "Firstname name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Firstname must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message="Lastname cannot be blank.")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)$",
            message = "Lastname name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Lastname must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @ValidAge
    @Column(name = "dob", nullable = false)
    private LocalDate dob;

    @Pattern(regexp = "^[0-9]{8}$", message = "Phone must be 8 digits.")
    @Column(name = "phone", nullable = false)
    private String phone;

    @Email(message = "Please enter a valid email address.")
    @Column(name = "email", nullable = false)
    private String email;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
