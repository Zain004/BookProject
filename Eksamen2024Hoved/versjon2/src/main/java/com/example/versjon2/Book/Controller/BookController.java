package com.example.versjon2.Book.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.Book.BooksDTO;
import com.example.versjon2.Book.Entity.Book;
import com.example.versjon2.Book.Service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping("/api/books")
public class BookController {
    private final Logger logger = LoggerFactory.getLogger(BookController.class);
    private final BookService bookService;

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
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        logger.info("Received request to update book with id : {} AND year: {}", id, newYear);

        Book updateBook = bookService.updateBookYear(id, newYear);
        BooksDTO booksDTO = BooksDTO.convertToDTO(updateBook);

        logger.info("RequestId: {} - Updated Successfully Book: " + updateBook);

        return APIResponse.okResponse(booksDTO, "Updated book ID: " + id + " with new year: " + newYear);
    }



    @DeleteMapping("/deleteBook/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            logger.info("Recieved request to delete book with ID : " + id);
            bookService.deleteBookById(id);
            return ResponseEntity.ok(Map.of(
                    "success","Book with ID " + id + " successfully deleted"));
        } catch (RuntimeException e) {
            logger.error("Error deletig book: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message","Error: " + e.getMessage()));
        }
    }
    @GetMapping("/bookStatistics")
    public ResponseEntity<String> getBookStatistics() {
        try {
            List<Book> books = bookService.getAllBooks();
            if(books == null || books.isEmpty()) {
                return ResponseEntity.badRequest().body("No books available.");
            }
            String statistics = bookService.getBookStatistics(books);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching book statistics: ", e);
            return ResponseEntity.badRequest().body("Error fetching book statistics: " + e.getMessage());
        } catch (Exception e) { // håndterer uventede feil
            logger.error("Unexpected error occured: ", e);
           return ResponseEntity.badRequest().body("Unexpected error occured; " + e); // returnerer generiks feilmelding
        }
    }

    @DeleteMapping("/deletePoetryBooks")
    public ResponseEntity<?> deletePoetryBooks(HttpServletRequest request) {
    try {
        //Hent session fra request
        HttpSession session = request.getSession(false);
        if(session == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "message","User is not logged in."
            ));
        }
        int deleteCount = bookService.deletePoetryBooksPublishedAfter2000(session);
        if(deleteCount > 0) {
            return ResponseEntity.ok(
                    Map.of(
                            "success","Deleted " + deleteCount + " poetry books published after 2000"));
        } else {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                 "message","No books found to delete"));
        }
    } catch (RuntimeException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "message","User is not logged in."));
    } catch (Exception e) {
        logger.error("An error occured while deleting books: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message","An error occured while deleting books."));
    }
}
}
