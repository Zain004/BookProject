package com.example.versjon2.Book.Service;

import com.example.versjon2.Book.BooksDTO;
import com.example.versjon2.Book.Entity.Book;
import com.example.versjon2.Book.Repository.BookRepository;
import com.example.versjon2.Authentication.Service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
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

    public String getBookStatistics(List<Book> books) {

        if(books == null || books.isEmpty()) {
            throw new IllegalArgumentException("Book List cannot be null or empty. ");
        }

        // Metode 1: Beregn antall bøker i listen
        int bookCount = books.size(); // Antall bøker
        // Opprett en Map for å holde oversikt over antall bøker per forfatter
        Map<String, Integer> authorCountMap = new HashMap<>();
        for(Book book : books) {
            // Hvis forfatteren finnes, øk antallet med 1, ellers sett til 1
            authorCountMap.put(book.getAuthor(), authorCountMap.getOrDefault(book.getAuthor(),0) + 1);
        }
        // Metode 2: Finn den eldste boken ved å sammenligne publiseringsåret
        Book oldestBook = books.stream()
                .min((book1, book2) -> Integer.compare(book1.getPublishingYear(), book2.getPublishingYear()))
                .orElseThrow(() -> new RuntimeException("No books available. ")); // Kaster unntak hvis listen er tom
        // Metode 3: Finn forfattere som kommer mer enn 1 ganger
        String authorsAppearMoreThanOnce = authorCountMap.entrySet().stream()
                .filter(objekt -> objekt.getValue() > 1) // Filtrer ut forfattere som har mer enn en bok
                .map(Map.Entry::getKey) // Hent forfatternavnene (nøklene)
                .reduce((book1, book2) -> (book1 + ", " + book2)) // slå sammen forfatterNavnene til en streng
                .orElse("No Authors appear more than once. "); // Hvis ingen forfattere har mer enn en bok vis denne meldingen
        // Metode 4: Finn forfatteren med flest bøker
        int maxBooks = authorCountMap.entrySet().stream()
                .max((author1, author2) -> Integer.compare(author1.getValue(), author2.getValue())) // finn den største verdien
                .map(Map.Entry::getValue) // hent antall bøker for dne mest frekvente forfatteren
                .orElse(0); // Hvis ingen bøker finnes, returner 0 som standard
        // Finn alle forfattere som har det maksimale antallet bøker
        List<String> authorsWithMostBooks = authorCountMap.entrySet().stream() // filtrer forfattere med samme antall bøker
                .filter(objekt -> objekt.getValue() == maxBooks) // hent forfatternavnene
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()); // samle dem i en lsite
        // hvis flere forfattere har flest bøker, ramse dem opp
        String mostFrequentAuthors = String.join(", ", authorsWithMostBooks);

    // Returner resultatet som en formatert streng
        return String.format("We have %d books. The oldest book is %s by %s publishes in %d. " +
            "Authors that appears more than Once: '%s'. The Author with most books is %s",
            bookCount, oldestBook.getTitle(), oldestBook.getAuthor(), oldestBook.getPublishingYear(),
                authorsAppearMoreThanOnce, mostFrequentAuthors
        );
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

}

