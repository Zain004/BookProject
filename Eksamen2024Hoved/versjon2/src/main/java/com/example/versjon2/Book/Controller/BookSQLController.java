package com.example.versjon2.Book.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.Authentication.Service.UserService;
import com.example.versjon2.Book.DTO.BookSQLDTO;
import com.example.versjon2.Book.Entity.BookSQL;
import com.example.versjon2.Book.Service.BookSQLService;
import com.example.versjon2.Book.statistics.BookSQLStatsDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/booksSQL")
public class BookSQLController {

    private final Logger logger = LoggerFactory.getLogger(BookSQLController.class);
    private final BookSQLService bookService;
    //private final AuthorService authorService;
    private final UserService userService;

    // LAg en side for testing

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<BookSQLDTO>> getBookByID(@PathVariable @NotNull Long id) {
        String requestId = MDC.get("requestId"); // hent fra MDC
        logger.info("Request ID: {} - Recieved request to fetch Book with Isbn ID: {}.", requestId, id);

        BookSQL bookSQL = bookService.getBook(id);

        logger.info("Request ID: {} - Successfully fetched Book with ID: {}, Book: {}", requestId, id, bookSQL);
        BookSQLDTO bookSQLDTO = BookSQLDTO.convertToDTO(bookSQL);

        return APIResponse.okResponse(bookSQLDTO, "Successfully fetched book with ID : " + id);
    }

    /**
     * returnerer liste med key
     * @return
     */
    @PostMapping("/saveBooks")
    public ResponseEntity<APIResponse<List<BookSQLDTO>>> saveBooks() {
        String requestId = MDC.get("requestId"); // hent fra MDC
        logger.info("Request ID: {} - Recieved request to save Users to DB.", requestId);

        List<BookSQL> books = new ArrayList<>();
        books.add(new BookSQL("Pride and Prejudice", "Jane Austen", 1813, 4.3, "Romance")); //OK
        books.add(new BookSQL("Animal Farm", "George Orwell", 1951, 4.1, "Satire")); //OK
        books.add(new BookSQL("The Lord of the Rings", "J.R.R. Tolkien", 1954, 4.6, "Fantasy"));//OK
        books.add(new BookSQL("Leaves of Grass", "Walt Whitman", 1855, 4.3, "Poetry")); //OK
        books.add(new BookSQL("Inferno", "Dante Alighieri", 1320, 4.7, "Poetry")); //OK

        List<BookSQL> bookSQL = bookService.saveBooks(books);
        bookSQL.forEach(book -> logger.info("Saving book: {" + book + "}"));

        List<BookSQLDTO> booksDTOs = BookSQLDTO.convertToDTOList(bookSQL);
        logger.info("Request ID: {} - Returning list of books to client. Saved successfully.", requestId);
        // implementer MDCFilter
        return APIResponse.buildResponse(HttpStatus.CREATED, "Books successfully created in DB", booksDTOs);
    }

    /**
     * denne returnerer integer, fordi batch ikke returnerer key, kan returnere resten av listen
     * @return
     */
    @PostMapping("/saveBooksBatch")
    public ResponseEntity<APIResponse<Integer>> saveBatchInt() {
        String requestId = MDC.get("requestId"); // hent fra MDC
        logger.info("Request ID: {} - Recieved request to save Users to DB.", requestId);

        List<BookSQL> books = new ArrayList<>();
        books.add(new BookSQL("Pride and Prejudice", "Jane Austen", 1813, 4.3, "Romance")); //OK
        books.add(new BookSQL("Animal Farm", "George Orwell", 1951, 4.1, "Satire")); //OK
        books.add(new BookSQL("The Lord of the Rings", "J.R.R. Tolkien", 1954, 4.6, "Fantasy"));//OK
        books.add(new BookSQL("Leaves of Grass", "Walt Whitman", 1855, 4.3, "Poetry")); //OK
        books.add(new BookSQL("Inferno", "Dante Alighieri", 1320, 4.7, "Poetry")); //OK

        Integer bookSQL = bookService.saveBatch(books);

        logger.info("Request ID: {} - Successfully saved {} books.", requestId, bookSQL);
        // implementer MDCFilter
        return APIResponse.buildResponse(HttpStatus.CREATED, "Successfully created " + bookSQL + " books.", bookSQL);
    }

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<BookSQLDTO>>> getBooks() {
        logger.info("Fetching all books from DB.");
        List<BookSQL> books = bookService.getAllBooksList();

        List<BookSQLDTO> booksDTOs = List.of();
        if (books.isEmpty()) {
            logger.info("No books found in DB, returning 204 no Content.");
            return APIResponse.noContentResponse("No books found.", booksDTOs);
        }

        booksDTOs = BookSQLDTO.convertToDTOList(books);
        logger.info("Returning list of books to client.");
        return APIResponse.okResponse(booksDTOs, "Books successfully retrieved from DB");
    }

    @PostMapping("/saveBooks2")
    public ResponseEntity<APIResponse<List<BookSQLDTO>>> saveBooks(@Valid @RequestBody @NotNull List<BookSQL> books) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Received request to save books: {}", requestId, books);

        List<BookSQL> bookSQLS = bookService.saveBooks(books);
        books.forEach(book -> logger.info("Saving book: {" + book + "}"));

        List<BookSQLDTO> booksDTOs = BookSQLDTO.convertToDTOList(bookSQLS);
        logger.info("Request ID: {} - Returning list of books to client. Saved successfully {}.", requestId, booksDTOs);

        return APIResponse.buildResponse(HttpStatus.CREATED, "Books successfully created in DB", booksDTOs);
    }


    @PutMapping("/updateBookYear/{id}")
    public ResponseEntity<APIResponse<BookSQLDTO>> updateBookYear(@PathVariable Long id, @RequestParam int newYear) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Received request to update book with id : {} AND year: {}",requestId, id, newYear);

        BookSQL updateBook = bookService.updateBookYear(id, newYear);
        BookSQLDTO booksDTO = BookSQLDTO.convertToDTO(updateBook);

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
    public ResponseEntity<APIResponse<String>> getBookStatistics( ) { //HttpServletRequest request) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Recieved request to fetch book statistics");

        /*logger.debug("Request ID: {} - Authenticating user.", requestId);
        userService.authenticate(request);
        logger.debug("Request ID: {} - User authenticated successfully.", requestId);
         */
        BookSQLStatsDTO statsDTO = bookService.getBookStatistics();
        logger.info("RequestId: {} - Successfully made statsDTO: {}", requestId, statsDTO);

        String bookStatistics = bookService.makeStatisticksFromDTO(statsDTO);
        logger.info("RequestId: {} - Successfully made statsDTO: {}", requestId, bookStatistics);

        return APIResponse.okResponse(null, bookStatistics);
    }
    /*

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

     */
}
