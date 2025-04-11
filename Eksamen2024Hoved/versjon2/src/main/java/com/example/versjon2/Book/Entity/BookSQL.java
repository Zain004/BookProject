package com.example.versjon2.Book.Entity;


import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class BookSQL {
    private Long isbnId;

    @NotBlank(message = "Title cannot be blank")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)$",
            message = "Tite name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Title must be between 2 and 50 characters")

    private String title;

    @NotBlank(message = "Author cannot be blank")
    @Pattern(regexp = "^(?!\\s)(?!.*\\s{2})[A-Za-z.\\- ]{2,50}(?<!\\s)$",
            message = "Author name must contain only letters, spaces, dots, and dashes, and no leading or trailing spaces.")
    @Size(min = 2, max = 50, message = "Author must be between 2 and 50 characters")
    private String author;

    @Min(value = 1000, message = "Publishing year must be at least 1000")
    @Max(value = 2100, message = "Publishing year must be no later than 2100")
    @NotNull(message = "Publishing year cannot be null")
    private int publishingYear;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must be no more than 5.0")
    @NotNull(message = "Rating cannot be null")
    private double rating;

    @NotBlank(message = "Category cannot be blank")
    @Size(min = 2, max = 50, message = "Category must be between 2 and 50 characters")
    private String category;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
