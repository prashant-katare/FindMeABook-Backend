package com.BRS.BookRecomendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.BRS.BookRecomendation.Entities.Order;
import com.BRS.BookRecomendation.Entities.UserInfo;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private UserInfoService userInfoService;

    // In a real application, you would inject an email service or other
    // notification mechanism

    public void sendOrderConfirmation(Order order) {
        logger.info("Preparing order confirmation notification for order ID: {}", order.getId());

        UserInfo user = userInfoService.getUserById(order.getUser().getId());
        if (user == null) {
            logger.warn("Cannot send order confirmation - user not found for username: {}",
                    order.getUser().getUsername());
            return;
        }

        // In a real application, you would send an actual email
        logger.info("Sending order confirmation email to: {}", user.getUsername());
        logger.debug("Order confirmation details - Order ID: {}, Total: ${}",
                order.getId(), order.getTotalPrice());

        // Simulated email sending
        System.out.println("Sending order confirmation email to: " + user.getUsername());
        System.out.println("Order ID: " + order.getId());
        System.out.println("Total: $" + order.getTotalPrice());

        logger.info("Order confirmation notification sent successfully to: {}", user.getUsername());
    }

    public void sendOrderStatusUpdate(Order order) {
        logger.info("Preparing order status update notification for order ID: {}", order.getId());

        UserInfo user = userInfoService.getUserById(order.getUser().getId());
        if (user == null) {
            logger.warn("Cannot send status update - user not found for username: {}",
                    order.getUser().getUsername());
            return;
        }

        // In a real application, you would send an actual email
        logger.info("Sending order status update email to: {}", user.getUsername());
        logger.debug("Order status update details - Order ID: {}, Status: {}",
                order.getId(), order.getStatus());

        // Simulated email sending
        System.out.println("Sending order status update email to: " + user.getUsername());
        System.out.println("Order ID: " + order.getId());
        System.out.println("Status: " + order.getStatus());

        logger.info("Order status update notification sent successfully to: {}", user.getUsername());
    }

    public void sendStockNotification(Long userId, Long bookId, String bookTitle) {
        logger.info("Preparing stock notification for user ID: {} about book: {}", userId, bookTitle);

        UserInfo user = userInfoService.getUserById(userId);
        if (user == null) {
            logger.warn("Cannot send stock notification - user not found for user ID: {}", userId);
            return;
        }

        // In a real application, you would send an actual email
        logger.info("Sending stock notification email to: {}", user.getUsername());
        logger.debug("Stock notification details - Book: {} (ID: {})", bookTitle, bookId);

        // Simulated email sending
        System.out.println("Sending stock notification email to: " + user.getUsername());
        System.out.println("Book: " + bookTitle + " (ID: " + bookId + ") is now in stock!");

        logger.info("Stock notification sent successfully to: {}", user.getUsername());
    }

    public void sendWelcomeEmail(Long userId) {
        logger.info("Preparing welcome email for new user ID: {}", userId);

        UserInfo user = userInfoService.getUserById(userId);
        if (user == null) {
            logger.warn("Cannot send welcome email - user not found for user ID: {}", userId);
            return;
        }

        // In a real application, you would send an actual email
        logger.info("Sending welcome email to: {}", user.getUsername());
        logger.debug("Welcome email details - User: {}, Full Name: {}",
                user.getUsername(), user.getFullName());

        // Simulated email sending
        System.out.println("Sending welcome email to: " + user.getUsername());
        System.out.println("Welcome to our Book Recommendation System, " + user.getFullName() + "!");

        logger.info("Welcome email sent successfully to: {}", user.getUsername());
    }
}