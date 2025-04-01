package com.example.versjon2.Book;

import com.example.versjon2.Book.Entity.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SecondaryRow;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BooksDTO {
    private Long isbnId;
    private String title;
    private String author;
    private int publishingYear;
    private Double rating;
    private String category;

    public static BooksDTO convertToDTO(Book book) {
        return new BooksDTO(
                book.getIsbnId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublishingYear(),
                book.getRating(),
                book.getCategory()
        );
    }

    public static List<BooksDTO> convertToDTOList(List<Book> book) {
        List<BooksDTO> dtos = new ArrayList<>();
        for (Book b : book) {
            dtos.add(convertToDTO(b));
        }
        return dtos;
    }
}
