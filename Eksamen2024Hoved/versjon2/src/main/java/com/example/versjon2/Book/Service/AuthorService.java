package com.example.versjon2.Book.Service;

import com.example.versjon2.Book.AuthorCount;
import com.example.versjon2.Book.Entity.Book;
import com.example.versjon2.Book.Repository.BookRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class AuthorService {
    private final BookRepository bookRepository;

    public Long findTotalBookCount() {
        return bookRepository.count();
    }

    public Map<String, Long> findAuthorCountMap() {
        List<AuthorCount> results = bookRepository.countBooksByAuthor();
        Map<String, Long> authorCountMap = results.stream()
                .collect(Collectors.toMap(AuthorCount::getAuthor, AuthorCount::getBookCount));
        return authorCountMap;
    }

    public Optional<Book> findOldestBook() {
        return bookRepository.findOldestBook();
    }

    public Map<String, Long> findAuthorsAppearingMoreThanOnce() {
        List<AuthorCount> results = bookRepository.findAuthorsAppearingMoreThanOnce();
        Map<String, Long> authorAppearing = results.stream()
                .collect(Collectors.toMap(AuthorCount::getAuthor, AuthorCount::getBookCount));
        return authorAppearing;
    }

    public List<AuthorCount> findAuthorsWithMostBooks() {
        List<AuthorCount> results = bookRepository.findAuthorWithMostBooks();
        StringBuilder authorsCountString = new StringBuilder();
        List<AuthorCount> topAuthors = List.of();
        if (results != null && !results.isEmpty()) {
            long maxBookCount = results.get(0).getBookCount();
            // finner alle forfatterne med mest bøker likt antall
            topAuthors = results.stream()
                    .filter(authorCount -> authorCount.getBookCount() == maxBookCount)
                    .collect(Collectors.toList());
        }
        return topAuthors;
    }
}
