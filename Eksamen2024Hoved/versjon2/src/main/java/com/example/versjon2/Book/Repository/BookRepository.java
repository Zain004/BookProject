package com.example.versjon2.Book.Repository;

import com.example.versjon2.Book.Entity.Book;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findById(Long id);
    @Modifying
    @Transactional
    @Query("DELETE FROM Book b WHERE b.category = :category AND b.publishingYear > :publishingYear")
    int deleteBooksByCategoryAndPublishingYearGreaterThan(String category, int publishingYear);

}
