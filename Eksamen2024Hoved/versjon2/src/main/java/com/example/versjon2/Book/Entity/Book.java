package com.example.versjon2.Book.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@NoArgsConstructor(force = true)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Entity
@ToString(exclude = {"created_at", "updated_at"})
@Table(name = "book",
        indexes = {
            @Index(name = "idx_title", columnList = "title"),
            @Index(name = "idx_category", columnList = "category"),
            @Index(name = "idx_publishingyear", columnList = "publishing_Year")
        }
)
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "isbnid", updatable = false, nullable = false)
    private Long isbnId;

    @NotBlank(message = "Title cannot be blank")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.: -]{2,50}(?<!\\s)$",
            message = "Tite name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Title must be between 2 and 50 characters")
    @Column(name = "title", nullable = false, length = 50)
    private String title;

    @NotBlank(message = "Author cannot be blank")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z. -]{2,50}(?<!\\s)$",
            message = "Author name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Author must be between 2 and 50 characters")
    @Column(name = "author", nullable = false, length = 50)
    private String author;

    @Min(value = 1000, message = "Publishing year must be at least 1000")
    @Max(value = 2100, message = "Publishing year must be no later than 2100")
    @NotNull(message = "Publishing year cannot be null")
    @Column(name = "publishing_year")
    private int publishingYear;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must be no more than 5.0")
    @NonNull
    private double rating;

    @NotBlank(message = "Category cannot be blank")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    @Column(length = 50, nullable = false)
    private String category;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

// denne konstruktøren er lagetr manuelt fordi vi ønsker å sette inn via server og ikke i data,sql
   public Book(String title, String author, int publishingYear, Double rating, String category) {
        this.title = title;
        this.author = author;
        this.publishingYear = publishingYear;
        this.rating = rating;
        this.category = category;
    }
}
