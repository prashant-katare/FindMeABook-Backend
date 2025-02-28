package com.BRS.BookRecomendation.Entities;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import jakarta.persistence.*;

@Entity
@Table(name = "books")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private String description;
    private String genreTag;

    private double price;

    private String imageUrl;
    private double rating;
    private int stockQuantity;

    private Long genreId;
}
