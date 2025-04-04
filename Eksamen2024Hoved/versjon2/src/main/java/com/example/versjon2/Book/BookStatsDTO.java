package com.example.versjon2.Book;

import com.example.versjon2.Book.Entity.Book;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookStatsDTO {
    private Long totalBooks;
    private Map<String, Long> authorCountMap;
    private Book oldestBook;
    private Map<String, Long> authorAppearingMoreThanOnce;
}
