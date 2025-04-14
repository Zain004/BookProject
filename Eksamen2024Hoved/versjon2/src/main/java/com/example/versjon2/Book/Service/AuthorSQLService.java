package com.example.versjon2.Book.Service;

import com.example.versjon2.Book.statistics.AuthorCount;
import com.example.versjon2.Book.Entity.BookSQL;
import com.example.versjon2.Book.Repository.BookRepository;
import com.example.versjon2.Book.Repository.BookSQLRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class AuthorSQLService {
    private final BookRepository bookRepository;
    private final Logger logger = LoggerFactory.getLogger(AuthorSQLService.class);

    private JdbcTemplate jdbcTemplate;

    public Long findTotalBookCount() {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Attempting finding Total Book Count.", requestId);

        String sql = "SELECT COUNT(*) FROM BOOKSQL";
        Long count =  jdbcTemplate.queryForObject(sql, Long.class);
        logger.info("Request ID: {} - Total Book Count: {}", requestId, count);

        return count;
    }

    public Map<String, Long> findAuthorCountMap() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting finding Author Count Map", requestId);

        String sql = "SELECT author, COUNT(*) AS bookCount FROM BOOKSQL GROUP BY author";

        List<AuthorCount> results = jdbcTemplate.query(sql, this::mapAuthorCount);

        logger.info("Request ID: {} - Author count map: {}", requestId, results);
        Map<String, Long> authorCountMap = results.stream()
                .filter(authorCount -> authorCount.getAuthor() != null) // Filtrer bort
                .collect(Collectors.toMap(AuthorCount::getAuthor, AuthorCount::getBookCount));

        logger.info("Author count map: {}", authorCountMap);
        return authorCountMap;
    }

    public Optional<BookSQL> findOldestBook() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting finding oldest book from DB.", requestId);

        String sql = "SELECT * FROM BOOKSQL ORDER BY publishing_year ASC LIMIT 1";
        BookSQL bookSQL = jdbcTemplate.queryForObject(sql, BookSQLRepository.bookSQLRowMapper);

        logger.info("Request ID: {} - Recieved Book SQL: {}", requestId, bookSQL);
        return Optional.of(bookSQL);
    }

    public Map<String, Long> findAuthorsAppearingMoreThanOnce() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting finding Authors that appears more than once.", requestId);

        String sql = "SELECT author, COUNT(*) AS bookCount FROM BOOKSQL GROUP BY author";
        List<AuthorCount> results = jdbcTemplate.query(sql, this::mapAuthorCount);

        logger.info("Request ID: {} - Author count map: {}", requestId, results);
        Map<String, Long> authorAppearing = results.stream()
                .collect(Collectors.toMap(AuthorCount::getAuthor, AuthorCount::getBookCount));

        return authorAppearing;
    }

    public List<AuthorCount> findAuthorsWithMostBooks() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting finding Authors with most Books.", requestId);

        String sql = "SELECT author, COUNT(*) AS bookCount FROM BOOKSQL GROUP BY author ORDER BY bookCount DESC";
        List<AuthorCount> results = jdbcTemplate.query(sql, this::mapAuthorCount);

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

    private AuthorCount mapAuthorCount(ResultSet rs, int rowNum) throws SQLException {
        String author = rs.getString("author");
        Long bookCount = rs.getLong("bookCount");
        return new AuthorCount(author, bookCount);
    }
}
