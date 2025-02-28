package com.BRS.BookRecomendation.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long bookId;
    private String title;
    private String author;
    private String imageUrl;
    private double price;
    private Integer quantity;
    private LocalDateTime addedDate;
}