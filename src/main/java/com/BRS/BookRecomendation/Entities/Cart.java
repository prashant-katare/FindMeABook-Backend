package com.BRS.BookRecomendation.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Data
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    private Integer quantity;

    @Column(name = "added_date")
    private java.time.LocalDateTime addedDate;
}