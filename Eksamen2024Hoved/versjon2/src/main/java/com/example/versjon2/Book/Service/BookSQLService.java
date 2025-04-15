package com.example.versjon2.Book.Service;

import com.example.versjon2.Authentication.Service.UserService;
import com.example.versjon2.Book.Entity.Book;
import com.example.versjon2.Book.statistics.AuthorCount;
import com.example.versjon2.Book.statistics.BookSQLStatsDTO;
import com.example.versjon2.Book.statistics.BookStatsDTO;
import com.example.versjon2.Book.Entity.BookSQL;
import com.example.versjon2.Book.Repository.BookSQLRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.*;

@AllArgsConstructor
@Service
public class BookSQLService {
    private final BookSQLRepository bookRepository;

    private final Logger logger = LoggerFactory.getLogger(BookSQLService.class);

    private final AuthorSQLService authorService;

    private final UserService userService;
    private final BookSQLRepository bookSQLRepository;

    private JdbcTemplate jdbcTemplate;


    @Transactional(readOnly = true)
    public BookSQL getBook(Long id) {
        String requestId = MDC.get("requestId"); // hent fra MDC
        logger.info("Request ID: {} - Attempting to fetch Book with ID: {}.", requestId, id);

        Assert.notNull(id, "ID cannot be null.");
        Optional<BookSQL> bookSQL = bookRepository.getBookById(id);

        if (bookSQL.isEmpty()) {
            logger.info("Request ID: {} - Attempted to fetch book but no book with id: {} was found.", requestId, id);
            throw new EmptyResultDataAccessException("Book with id " + id + " not found.",1);
        }

        logger.info("Requst ID: {} - Successfully fetched book with id {}, Book: {}.", requestId, id, bookSQL);
        return bookSQL.get();
    }

    @Transactional
    public List<BookSQL> saveBooks(@NonNull List<BookSQL> books) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Attempting to save books: {}", requestId, books);
        Assert.notNull(books, "Book list cannot be null.");

        if (books.isEmpty()) {
            logger.info("Request ID: {} - Recieved an empty book list. No books will be saved.", requestId);
            throw new IllegalArgumentException("No books will be saved.");
        }

        logger.info("RequestId: {} - Attempting saving {} books.", requestId, (Integer) books.size());
        return bookRepository.saveAll(books);
    }

    @Transactional
    public int saveBatch(@NonNull List<BookSQL> books) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Attempting to save books: {}", requestId, books);
        Assert.notNull(books, "Book list cannot be null.");

        if (books.isEmpty()) {
            logger.info("Request ID: {} - Recieved an empty book list. No books will be saved.", requestId);
            throw new IllegalArgumentException("No books will be saved.");
        }

        logger.info("RequestId: {} - Attempting saving {} books.", requestId, (Integer) books.size());
        return bookRepository.saveBatch(books);
    }


    @Transactional(readOnly = true)
    public List<BookSQL> getAllBooksList() {
        logger.info("Attemprign to Fetch all books from DB.");
        List<BookSQL> books = bookRepository.findAllBooksList();

        if(books.isEmpty()) {
            logger.info("No books found in DB.");
            return Collections.emptyList();
        }

        logger.info("Retrieved {} books from DB.", books.size());
        return books;
    }


    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 10)
    public BookSQL updateBookYear(Long id, int newYear) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.debug("Request ID: {} - Attempting to update book with id: {} to newYear: {}", requestId, id, newYear);
        Assert.notNull(id, "Book id cannot be null.");

        BookSQL bookSQL = bookRepository.updateBookById(id, newYear);

        if (bookSQL == null) {
            logger.info("Request ID: {} - Attempted to update book but no book with id: {} was found.", requestId, id);
            throw new EmptyResultDataAccessException("Book with id " + id + " not found for updating year.",1);
        }

        logger.info("Request ID: {} - Successfully updated book with id: {} to newYear: {}, BOOKEntity: {}", requestId, id, newYear, bookSQL);
        return bookSQL;
    }


    @Transactional
    public void deleteBookById(Long id) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting to delete book with id: {}", requestId, id);
        Assert.notNull(id, "Book id cannot be null.");

        Integer count = bookRepository.getCountByID(id);

        if (count == null || count == 0) {
            logger.warn("Request ID: {} - Attempted to retrieve a non-existent book with ID: {}", requestId, id);
            throw new NoSuchElementException("Book with ID " + id + " does not exist");
        }

        logger.info("Request ID: {} - Successfully confirmed book with ID: {} exists before deleting.", requestId, id);
        int rowsAffected = bookRepository.deleteByID(id);

        if (rowsAffected == 1) {
            logger.info("Request ID: {} - Successfully deleted book with ID: {}, (rows affected: {}).", requestId, id, rowsAffected);
        } else if (rowsAffected == 0) {
            logger.warn("Request ID: {} - No book found with ID: {} to delete.  Possible race condition or data integrity issue.", requestId, id);
        } else {
            logger.error("Request ID: {} - Unexpected number of rows ({}) affected when deleting book with ID: {}.  Data corruption likely!", requestId, rowsAffected, id);
        }
    }


    public BookSQLStatsDTO getBookStatistics() {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Recieved request for generating statistics.", requestId);


        BookSQLStatsDTO statsDTO = new BookSQLStatsDTO();

        long totalBooks = authorService.findTotalBookCount();

        if (totalBooks == 0) {
            logger.warn("No books found in the database. Returning null.");
            return null; // Tidlig retur hvis ingen bøker
        }
        // 1. returner antall elementer
        statsDTO.setTotalBooks(totalBooks);

        // 2. Antall bøker per forfatter
        statsDTO.setAuthorCountMap(authorService.findAuthorCountMap());

        // Metode 3: Finn den eldste boken ved å sammenligne publiseringsåret
        statsDTO.setOldestBook(authorService.findOldestBook());

        // Metode 4: Finn forfattere som kommer mer enn 1 ganger
        statsDTO.setAuthorAppearingMoreThanOnce(authorService.findAuthorsAppearingMoreThanOnce());

        // Metode 5: Finn forfattere som har det maksimale antallet bøker blant alle forfattere
        statsDTO.setAuthorWithMostBooks(authorService.findAuthorsWithMostBooks());

        return statsDTO;
    }

    public String makeStatisticksFromDTO(BookSQLStatsDTO statsDTO) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Making statisticks from DTO: {}", requestId, statsDTO);

        StringBuilder result = new StringBuilder();

        if (statsDTO.getTotalBooks() == 0) {
            logger.warn("No books found in the database.");
            result.append("No books found in the database."); // Tidlig retur hvis ingen bøker
        }

        // 1. returner antall elementer
        logger.info("Successfully counted total books: {}.", statsDTO.getTotalBooks());
        result.append("We have ").append(statsDTO.getTotalBooks()).append(" books.\n");

        // 2. Antall bøker per forfatter
        StringBuilder authorsCountString = new StringBuilder();
        authorsCountString.append("Number of books per author: \n");
        statsDTO.getAuthorCountMap().forEach((author, count) -> {
            authorsCountString.append(author).append(": ").append(count).append("\n");
        });
        result.append("number og books per author: ").append(authorsCountString);

        // Metode 3: Finn den eldste boken ved å sammenligne publiseringsåret
        Optional<BookSQL> oldestBook = statsDTO.getOldestBook();
        result.append("The oldest book is: ").append(oldestBook.get().getAuthor()).append(", published on ").append(oldestBook.get().getPublishingYear()).append(".\n");

        // Metode 4: Finn forfattere som kommer mer enn 1 ganger
        StringBuilder authorsAppearingString = new StringBuilder();
        authorsAppearingString.append("Authors Appearing More Than once: \n");

        statsDTO.getAuthorAppearingMoreThanOnce().forEach((author, count) -> {
            authorsAppearingString.append(author).append(": ").append(count).append("\n");
        });

        result.append(authorsAppearingString);

        // Metode 5: Finn forfattere som har det maksimale antallet bøker blant alle forfattere
        List<AuthorCount> topAuthors = statsDTO.getAuthorWithMostBooks();
        StringBuilder sb = new StringBuilder();
        // looper igjennom forfattere med mest bøker
        for (int i =0; i < topAuthors.size() ; i++) {
            AuthorCount authorCount = topAuthors.get(i); // henter forfatter med bok
            sb.append(authorCount.getAuthor())
                    .append(" has ").append(authorCount.getBookCount()).append(" books.");
            if (i < topAuthors.size() - 1) {
                sb.append(", "); // Legg til komma hvis det er flere forfattere
            }
        }
        statsDTO.setAuthorWithMostBooks(topAuthors);
        result.append(sb);
        return result.toString();
    }

    @Transactional
    public int deleteBooksWithCategoryAndPublishingYearGreatherThan(String category, int publishing_year) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting deleting Boooks with Category: {}, AND publishing_year Greather than: {}.", requestId, category, publishing_year);
        return bookSQLRepository.deleteBooksWithCategoryAndPublishingYearGreatherThanEquals(category, publishing_year);
    }




}

