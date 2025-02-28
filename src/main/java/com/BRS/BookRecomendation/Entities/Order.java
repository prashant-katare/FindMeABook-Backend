package com.BRS.BookRecomendation.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Foreign key reference to UserInfo
    private String username;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private List<OrderItem> orderItems;

    private Double totalPrice;
    private String status; // "Pending", "Shipped", "Delivered"
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}

