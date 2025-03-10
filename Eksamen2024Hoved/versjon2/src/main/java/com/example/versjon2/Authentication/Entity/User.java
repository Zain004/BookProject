package com.example.versjon2.Authentication.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//import javax.validation.constraints.NotBlank;


@NoArgsConstructor
@Entity
@Table(name = "app_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long userId;

    @Getter
    @Setter
    @NotBlank
    private String username;

    @Column(name="password_Hash", nullable = false) // kan ikke være null
    @Getter // ingen setter for å hindre uønskede opppdateringer
    @NotBlank
    private String passwordHash; // Hash av passordet, ikke klartekst

    @Getter
    @Setter
    private boolean loggedIn;

    public void updateHashPassword(String rawPassord, BCryptPasswordEncoder passwordEncoder) {
        this.passwordHash = passwordEncoder.encode(rawPassord);
    }
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.loggedIn = false;
    }
}
