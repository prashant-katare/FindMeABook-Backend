package com.BRS.BookRecomendation.Entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlists")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserInfo user;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(name = "added_date")
    private java.time.LocalDateTime addedDate;
}