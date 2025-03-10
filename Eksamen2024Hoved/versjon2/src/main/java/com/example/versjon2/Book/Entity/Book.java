package com.example.versjon2.Book.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Data
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long isbnId;
    private String title;
    private String author;
    private int publishingYear;
    private Double rating;
    private String category;
// denne konstruktøren er lagetr manuelt fordi vi ønsker å sette inn via server og ikke i data,sql
   public Book(String title, String author, int publishingYear, Double rating, String category) {
        this.title = title;
        this.author = author;
        this.publishingYear = publishingYear;
        this.rating = rating;
        this.category = category;
    }
}
