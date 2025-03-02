package com.BRS.BookRecomendation.Entities;

import com.BRS.BookRecomendation.DTO.Status;
import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference  // Prevents infinite recursion
    private List<OrderItem> orderItems;
    
    private String username;

    private Double totalPrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
