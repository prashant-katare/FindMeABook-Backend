package com.BRS.BookRecomendation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.Cart;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.repository.BookRepository;
import com.BRS.BookRecomendation.repository.CartRepository;
import com.BRS.BookRecomendation.repository.UserInfoRepository;

@Service
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private BookRepository bookRepository;

    // Cart operations
    public void addToCart(Long userId, Long bookId, Integer quantity) {
        logger.info("Attempting to add book ID: {} with quantity: {} to cart for user: {}", bookId, quantity, userId);

        try {
            Cart existingItem = cartRepository.findByUserIdAndBookId(userId, bookId);

            if (existingItem != null) {
                logger.info("Book already in cart, updating quantity from {} to {}", existingItem.getQuantity(),
                        existingItem.getQuantity() + quantity);
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                cartRepository.save(existingItem);
            } else {
                UserInfo user = userInfoRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("Book not found"));

                Cart cart = Cart.builder()
                        .user(user)
                        .book(book)
                        .quantity(quantity)
                        .addedDate(LocalDateTime.now())
                        .build();

                cartRepository.save(cart);
                logger.info("Successfully added book ID: {} to cart for user: {}", bookId, userId);
            }
        } catch (Exception e) {
            logger.error("Failed to add book to cart: {}", e.getMessage());
            throw e;
        }
    }

    public void updateCartQuantity(Long userId, Long bookId, Integer quantity) {
        logger.info("Attempting to update quantity to {} for book ID: {} in cart for user: {}", quantity, bookId,
                userId);

        try {
            Cart cart = cartRepository.findByUserIdAndBookId(userId, bookId);
            if (cart != null) {
                logger.info("Updating quantity from {} to {} for book ID: {} in user: {}'s cart",
                        cart.getQuantity(), quantity, bookId, userId);
                cart.setQuantity(quantity);
                cartRepository.save(cart);
                logger.info("Successfully updated quantity for book ID: {} in cart", bookId);
            } else {
                logger.warn("Cart item not found for user ID: {} and book ID: {}", userId, bookId);
            }
        } catch (Exception e) {
            logger.error("Failed to update cart quantity: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void removeFromCart(Long userId, Long bookId) {
        logger.info("Attempting to remove book ID: {} from cart for user: {}", bookId, userId);

        try {
            Cart cart = cartRepository.findByUserIdAndBookId(userId, bookId);
            if (cart != null) {
                cartRepository.delete(cart);
                logger.info("Successfully removed book ID: {} from cart for user: {}", bookId, userId);
            } else {
                logger.warn("Cart item not found for user ID: {} and book ID: {}", userId, bookId);
            }
        } catch (Exception e) {
            logger.error("Failed to remove book from cart: {}", e.getMessage());
            throw e;
        }
    }

    public List<Cart> getUserCart(Long userId) {
        logger.info("Retrieving cart contents for user: {}", userId);

        try {
            List<Cart> cartItems = cartRepository.findByUserId(userId);
            logger.info("Retrieved {} items from cart for user: {}", cartItems.size(), userId);
            return cartItems;
        } catch (Exception e) {
            logger.error("Failed to retrieve user cart: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void clearCart(Long userId) {
        logger.info("Removing all books from cart for user: {}", userId);

        try {
            // Find user by username
            UserInfo user = userInfoRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Delete all cart items for this user's ID
            int deletedCount = cartRepository.deleteByUserId(userId);
            logger.info("Successfully removed {} books from cart for user: {}", deletedCount, userId);
        } catch (Exception e) {
            logger.error("Error removing all books from cart: {}", e.getMessage());
            throw e;
        }
    }

    public Cart ifBookExistsInCart(Long userId, Long bookId) {
        return cartRepository.findByUserIdAndBookId(userId, bookId);
    }

}