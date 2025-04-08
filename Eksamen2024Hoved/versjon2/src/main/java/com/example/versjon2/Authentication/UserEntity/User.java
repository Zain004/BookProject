package com.example.versjon2.Authentication.UserEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table (
        name="app_user",
        indexes = {
                @Index(name = "idx_user_name", columnList = "user_name")
        }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long userId;

    @Getter @Setter @NotBlank
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.0-9@\\-]{2,50}(?<!\\s)$", message = "Username name must contain only letters, spaces, dots, digits, and dashes, and no leading or trailing spaces.")
    @Column(name = "user_name", nullable = false, length = 50)
    private String username;


    @Getter // ingen setter for å hindre uønskede opppdateringer
    @NotBlank
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character from '@$!%*?&'")
    @Column(name="password_Hash", nullable = false) // kan ikke være null
    private String passwordHash; // Hash av passordet, ikke klartekst
    public void updateHashPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        this.passwordHash = passwordEncoder.encode(rawPassword);
    }
    @CreatedDate
    @Column
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column
    private LocalDateTime updatedAt;

    @Getter @Setter
    private LocalDateTime lastLoggedIn;

    @Getter @Setter
    @ElementCollection(fetch = FetchType.EAGER) // Laster roller samtidig med brukeren
    @CollectionTable(name = "user_roler", joinColumns = @JoinColumn(name = "user_id")) // Tabell "user_roles" kobles via "user_id"
    @Column(name="role")
    private Set<Role> roles = new HashSet<>();

    public enum Role {
        USER,
        ADMIN,
        EMPLOYEE
    }

    public User(String username, String passwordHash, Set<Role> roles) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.roles = roles;
    }
}
