package com.BRS.BookRecomendation.DTO;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.BRS.BookRecomendation.Entities.OrderItem;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;
    private List<OrderItem> orderItems;
}
