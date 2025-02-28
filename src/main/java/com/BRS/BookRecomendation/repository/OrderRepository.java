package com.BRS.BookRecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BRS.BookRecomendation.Entities.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUsername(String username);
}
