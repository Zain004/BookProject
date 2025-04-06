package com.example.versjon2.Book.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.Authentication.Service.UserService;
import com.example.versjon2.Book.BookStatsDTO;
import com.example.versjon2.Book.BooksDTO;
import com.example.versjon2.Book.Entity.Book;
import com.example.versjon2.Book.Service.AuthorService;
import com.example.versjon2.Book.Service.BookService;
import com.example.versjon2.SecurityConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;
    private final AuthorService authorService;
    private final UserService userService;

    @GetMapping("/listBooks")
    public ResponseEntity<APIResponse<List<BooksDTO>>> getBooks() {
        logger.info("Fetching all books from DB.");
        List<Book> books = bookService.getAllBooks();

        List<BooksDTO> booksDTOs = List.of();
        if (books.isEmpty()) {
            logger.info("No books found in DB, returning 204 no Content.");
            return APIResponse.noContentResponse("No books found.", booksDTOs);
        }

        booksDTOs = BooksDTO.convertToDTOList(books);
        logger.info("Returning list of books to client.");
        return APIResponse.okResponse(booksDTOs, "Books successfully retrieved from DB");
    }

    @PostMapping("/saveBooks")
    public ResponseEntity<APIResponse<List<BooksDTO>>> saveBooks() {
        String requestId = MDC.get("requestId"); // hent fra MDC
        logger.info("Request ID: {} - Recieved request to save Users to DB.", requestId);

        List<Book> books = new ArrayList<>();
        books.add(new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925, 4.0, "Fiction"));
        books.add(new Book("To Kill a Mockingbird", "Harper Lee", 1960, 4.2, "Fiction"));
        books.add(new Book("1984", "George Orwell", 1949, 4.5, "Dystopian"));
        books.add(new Book("Pride and Prejudice", "Jane Austen", 1813, 4.3, "Romance"));
        books.add(new Book("Animal Farm", "George Orwell", 1951, 4.1, "Satire"));
        books.add(new Book("The Lord of the Rings", "J.R.R. Tolkien", 1954, 4.6, "Fantasy"));
        books.add(new Book("Leaves of Grass", "Walt Whitman", 1855, 4.3, "Poetry"));
        books.add(new Book("Inferno", "Dante Alighieri", 1320, 4.7, "Poetry"));

        bookService.saveBooks(books);
        books.forEach(book -> logger.info("Saving book: {" + book + "}"));

        List<BooksDTO> booksDTOs = BooksDTO.convertToDTOList(books);
        logger.info("Request ID: {} - Returning list of books to client. Saved successfully.", requestId);
        // implementer MDCFilter
        return APIResponse.buildResponse(HttpStatus.CREATED, "Books successfully created in DB", booksDTOs);
    }

    @PostMapping("/saveBooks2")
    public ResponseEntity<APIResponse<List<BooksDTO>>> saveBooks(@Valid @RequestBody @NotNull List<Book> books) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Received request to save books: {}", requestId, books);

        bookService.saveBooks(books);
        books.forEach(book -> logger.info("Saving book: {" + book + "}"));

        List<BooksDTO> booksDTOs = BooksDTO.convertToDTOList(books);
        logger.info("Request ID: {} - Returning list of books to client. Saved successfully {}.", requestId, booksDTOs);

        return APIResponse.buildResponse(HttpStatus.CREATED, "Books successfully created in DB", booksDTOs);
    }


    @PutMapping("/updateBookYear/{id}")
    public ResponseEntity<APIResponse<BooksDTO>> updateBookYear(@PathVariable Long id, @RequestParam int newYear) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Received request to update book with id : {} AND year: {}",requestId, id, newYear);

        Book updateBook = bookService.updateBookYear(id, newYear);
        BooksDTO booksDTO = BooksDTO.convertToDTO(updateBook);

        logger.info("RequestId: {} - Updated Successfully Book: " + updateBook);
        return APIResponse.okResponse(booksDTO, "Updated book ID: " + id + " with new year: " + newYear);
    }

    @DeleteMapping("/deleteBook/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Recieved request to delete book with ID: {}", requestId, id);

        bookService.deleteBookById(id);

        logger.info("Request ID: {} - Successfully deleted book with ID: {}", requestId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookStatistics")
    public ResponseEntity<APIResponse<String>> getBookStatistics(HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Recieved request to fetch book statistics");

        logger.debug("Request ID: {} - Authenticating user.", requestId);
        userService.authenticate(request);
        logger.debug("Request ID: {} - User authenticated successfully.", requestId);

        BookStatsDTO statsDTO = bookService.getBookStatistics();
        logger.info("RequestId: {} - Successfully made statsDTO: {}", requestId, statsDTO);

        String bookStatistics = bookService.makeStatisticksFromDTO(statsDTO);
        logger.info("RequestId: {} - Successfully made statsDTO: {}", requestId, bookStatistics);

        return APIResponse.okResponse(null, bookStatistics);
    }

    @PostMapping("/book-statistics")
    public ResponseEntity<APIResponse<String>> getBookStatistics(@RequestBody List<Book> books) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Recieved request to fetch book statistics");

        if (books == null || books.isEmpty()) {
            logger.info("RequestId: {} - No books to make statistics of", requestId);
            return APIResponse.okResponse(null, "No books provided for statistics.");
        }

        String stats = bookService.getBookStatisticsFromList(books);
        logger.info("RequestId: {} - Successfully made statistics from List: {}", requestId, stats);

        return APIResponse.okResponse(null, stats);
    }

    @DeleteMapping("/deletePoetryBooks")
    public ResponseEntity<APIResponse<Integer>> deletePoetryBooks(HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Recieved request to delete poetry books", requestId);

        logger.debug("Request ID: {} - Authenticating user.", requestId);
        userService.authenticate(request);
        logger.debug("Request ID: {} - User authenticated successfully.", requestId);

        int deleteCount = bookService.deletePoetryBooksPublishedAfter2000();

        if (deleteCount == 0) {
            logger.debug("Request ID: {} - No poetry books were deleted.", requestId);
            return APIResponse.buildResponse(HttpStatus.NO_CONTENT, "No poetry books were deleted.", deleteCount);
        }

        logger.info("Request ID: {} - Successfully deleted {} poetry books.", requestId, deleteCount);
        return APIResponse.okResponse(deleteCount,"Successfully deleted " + deleteCount + " poetry books.");
    }
}
