package com.example.versjon2.Book;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthorCount {
    private String author;
    private Long bookCount;
}
