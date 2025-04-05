package com.example.versjon2.Book.Service;

import com.example.versjon2.Book.AuthorCount;
import com.example.versjon2.Book.BookStatsDTO;
import com.example.versjon2.Book.Entity.Book;
import com.example.versjon2.Book.Repository.BookRepository;
import com.example.versjon2.Authentication.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;

    private final Logger logger = LoggerFactory.getLogger(BookService.class);

    private final AuthorService authorService;

    private final UserService userService;

    @Transactional
    public List<Book> saveBooks(@NonNull List<Book> books) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Recieved request to save books: {}", requestId, books);
        Assert.notNull(books, "Book list cannot be null.");

        if (books.isEmpty()) {
            logger.info("Request ID: {} - Recieved an empty book list. No books will be saved.", requestId);
            throw new IllegalArgumentException("No books will be saved.");
        }

        logger.info("RequestId - Attempting saving {} books.", requestId, books.size());
        return bookRepository.saveAll(books);
    }

    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        logger.info("Fetching all books from DB.");
        List<Book> books = bookRepository.findAll();

        if(books.isEmpty()) {
            logger.info("No books found in DB.");
            return Collections.emptyList();
        }

        logger.info("Retrieved {} books from DB.", books.size());
        return books;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 10)
    public Book updateBookYear(Long id, int newYear) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.debug("Request ID: {} - Attempting to update book with id: {} to newYear: {}", requestId, id, newYear);

        return bookRepository.findById(id)
                .map(book -> {
                    book.setPublishingYear(newYear);
                    return bookRepository.save(book);
                }).orElseThrow(() -> {
                    logger.warn("Request ID: {} - Book with id {} not found for updating year.", requestId, id);
                    throw new EmptyResultDataAccessException("Book with id " + id + " not found for updating year.", 1);
                });
    }

    @Transactional
    public void deleteBookById(Long id) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting to delete book with id: {}", requestId, id);
        Assert.notNull(id, "Book id cannot be null.");

        if (bookRepository.existsById(id)) {
            logger.warn("Request ID: {} - Attempted to delete a non-existent book with ID: {}", requestId, id);
            throw new NoSuchElementException("Book with ID " + id + " does not exist");
        }

        bookRepository.deleteById(id);
        logger.info("Request ID: {} - Successfully deleted book with ID: {}", requestId, id);
    }


    /**
     *
     * @param request
     * @return
     */
    public BookStatsDTO getBookStatistics(HttpServletRequest request) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Recieved request for generating statistics.", requestId);

        logger.info("Checking if user is signed in");
        userService.authenticate(request);

        BookStatsDTO statsDTO = new BookStatsDTO();

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

    public String makeStatisticksFromDTO(BookStatsDTO statsDTO) {
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
        Optional<Book> oldestBook = statsDTO.getOldestBook();
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
/*
    public String getBookStatisticsFromList(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return "No books provided.";
        }
        long totalBooks = books.size();

    }

    public int deletePoetryBooksPublishedAfter2000(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        // sjekk om brukeren er logger inn
        if(userId == null || !userService.isUserLoggedIn(session)) {
            logger.warn("User is not logged in.");
            throw new RuntimeException("USer is not logged in.");
        }
        // Hvis brukeren er logget inn, fortsett å slette bøker
        try {
            // Antall slettede bøker
            int deletedCount = bookRepository.deleteBooksByCategoryAndPublishingYearGreaterThan("Poetry",2000);
            logger.info("Deleted {} poetry books published after 2000", deletedCount);
            return deletedCount;
        } catch (RuntimeException e) {
            logger.error("Runtime error while deleting books: " + e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Error while deleting books: " + e);
            throw new RuntimeException("An error occured while deleting books.");
        }
    }

 */

}

