package com.example.versjon2.Book.Repository;


import com.example.versjon2.AuthorCount;
import com.example.versjon2.Book.Entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    //Optional<Book> findById(Long id);
    @Modifying
    @Query("DELETE FROM Book b WHERE b.category = :category AND b.publishingYear > :publishingYear")
    int deleteBooksByCategoryAndPublishingYearGreaterThan(String category, int publishingYear);

    @Query("SELECT new com.example.versjon2.AuthorCount(b.author, COUNT(b)) FROM Book b GROUP BY b.author")
    List<AuthorCount> countBooksByAuthor();

    @Query("SELECT b FROM Book b ORDER BY b.publishingYear ASC LIMIT 1")
    Optional<Book> findOldestBook();

    @Query("SELECT new com.example.versjon2.AuthorCount(b.author, COUNT(b)) FROM Book b GROUP BY b.author HAVING COUNT(b) > 1")
    List<AuthorCount> findAuthorsAppearingMoreThanOnce();

    @Query("SELECT new com.example.versjon2.AuthorCount(b.author, COUNT(b)) FROM Book b GROUP BY b.author ORDER BY COUNT(b) DESC")
    List<AuthorCount> findAuthorWithMostBooks();
}
