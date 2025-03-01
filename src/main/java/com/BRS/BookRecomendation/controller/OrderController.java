package com.BRS.BookRecomendation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.BRS.BookRecomendation.Entities.Order;
import com.BRS.BookRecomendation.service.OrderService;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping("/{userId}/place")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> placeOrder(@PathVariable Long userId) {
        logger.info("Request to place order for User ID: {}", userId);
        try {
            Order order = orderService.placeOrder(userId);
            logger.info("Order successfully placed for User ID: {}, Order ID: {}", userId, order.getId());
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            logger.error("Failed to place order for User ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/getUserOrders")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        logger.info("Request to get orders for User ID: {}", userId);
        try {
            List<Order> orders = orderService.getOrdersByUserId(userId);
            logger.info("Retrieved {} orders for User ID: {}", orders.size(), userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving orders for User ID: {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @GetMapping("/{userId}/orderDetails/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long userId,
            @PathVariable Long orderId) {
        logger.info("Request to get order details for User ID: {}, Order ID: {}", userId, orderId);
        try {
            Order order = orderService.getOrdersByUserId(userId).stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst()
                    .orElse(null);

            if (order == null) {
                logger.warn("Order details not found for User ID: {}, Order ID: {}", userId, orderId);
                return ResponseEntity.notFound().build();
            }

            logger.info("Successfully retrieved order details for User ID: {}, Order ID: {}", userId, orderId);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            logger.error("Error retrieving order details for User ID: {}, Order ID: {}: {}", userId, orderId,
                    e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{orderId}/cancel")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId) {
        logger.info("Request to cancel Order ID: {}", orderId);
        try {
            Order order = orderService.cancelOrder(orderId);
            logger.info("Order ID: {} successfully cancelled", orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            logger.error("Failed to cancel Order ID: {}: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        logger.info("Admin request to get all orders");
        try {
            List<Order> orders = orderService.getAllOrders();
            logger.info("Retrieved {} orders for admin", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving all orders for admin: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/admin/{orderId}/status")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId,
            @RequestParam String status) {
        logger.info("Admin request to update Order ID: {} status to: {}", orderId, status);
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            logger.info("Order ID: {} status successfully updated to: {}", orderId, status);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            logger.error("Failed to update Order ID: {} status: {}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}