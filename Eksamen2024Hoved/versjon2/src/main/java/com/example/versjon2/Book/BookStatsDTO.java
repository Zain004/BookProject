package com.example.versjon2.Book;

import com.example.versjon2.AuthorCount;
import com.example.versjon2.Book.Entity.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookStatsDTO {
    private Long totalBooks;
    private Map<String, Long> authorCountMap;
    private Optional<Book> oldestBook;
    private Map<String, Long> authorAppearingMoreThanOnce;
    private List<AuthorCount> authorWithMostBooks;
}
