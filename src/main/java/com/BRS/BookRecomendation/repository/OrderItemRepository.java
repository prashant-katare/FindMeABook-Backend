package com.BRS.BookRecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BRS.BookRecomendation.Entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}
