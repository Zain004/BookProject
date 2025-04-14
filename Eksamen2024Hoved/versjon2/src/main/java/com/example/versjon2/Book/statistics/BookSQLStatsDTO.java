package com.example.versjon2.Book.statistics;

import com.example.versjon2.Book.Entity.BookSQL;
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
public class BookSQLStatsDTO {
    private Long totalBooks;
    private Map<String, Long> authorCountMap;
    private Optional<BookSQL> oldestBook;
    private Map<String, Long> authorAppearingMoreThanOnce;
    private List<AuthorCount> authorWithMostBooks;
}
