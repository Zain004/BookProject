package com.example.versjon2.Book.DTO;

import com.example.versjon2.Book.Entity.BookSQL;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BookSQLDTO {
    private Long isbnId;
    private String title;
    private String author;
    private int publishingYear;
    private double rating;
    private String category;

    public static BookSQLDTO convertToDTO(BookSQL book) {
        return new BookSQLDTO(
                book.getIsbnId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublishingYear(),
                book.getRating(),
                book.getCategory()
        );
    }

    public static List<BookSQLDTO> convertToDTOList(List<BookSQL> book) {
        List<BookSQLDTO> dtos = new ArrayList<>();
        for (BookSQL b : book) {
            dtos.add(convertToDTO(b));
        }
        return dtos;
    }
}
