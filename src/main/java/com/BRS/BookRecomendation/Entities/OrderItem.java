package com.BRS.BookRecomendation.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookId;
    private String bookTitle;
    private Integer quantity;
    private Double price;
    private String imageUrl;
}
