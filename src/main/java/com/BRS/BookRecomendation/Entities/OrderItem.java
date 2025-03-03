package com.BRS.BookRecomendation.Entities;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference  // Prevents infinite recursion
    private Order order;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    private String bookTitle;
    private Integer quantity;
    private Double price;
    private String imageUrl;
}
