package com.BRS.BookRecomendation.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.BRS.BookRecomendation.DTO.CartItemDTO;
import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.Cart;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.service.UserInfoService;
import com.BRS.BookRecomendation.service.CartService;
import com.BRS.BookRecomendation.service.BookService;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookService bookService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<CartItemDTO>> getUserCart(@PathVariable Long userId) {
        logger.info("Request to get cart for user ID: {}", userId);
        try {
            List<Cart> cartItems = cartService.getUserCart(userId);
            logger.debug("Retrieved {} cart items for user ID: {}", cartItems.size(), userId);

            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(item -> CartItemDTO.builder()
                            .bookId(item.getBook().getId())
                            .title(item.getBook().getTitle())
                            .author(item.getBook().getAuthor())
                            .imageUrl(item.getBook().getImageUrl())
                            .price(item.getBook().getPrice())
                            .quantity(item.getQuantity())
                            .addedDate(item.getAddedDate())
                            .build())
                    .collect(Collectors.toList());

            logger.info("Returning {} cart items for user ID: {}", cartItemDTOs.size(), userId);
            return ResponseEntity.ok(cartItemDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving cart for user ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{userId}/add")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> addToCart(@PathVariable Long userId,
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        logger.info("Request to add book ID: {} to cart for user ID: {} with quantity: {}", bookId, userId, quantity);
        try {
            // Check if book exists
            Book book = bookService.getBookById(bookId)
                    .orElseThrow(() -> {
                        logger.warn("Add to cart failed: Book with ID {} not found", bookId);
                        return new RuntimeException("Book not found");
                    });

            // Check if user exists
            UserInfo user = userInfoService.getUserById(userId);
            if (user == null) {
                logger.warn("Add to cart failed: User with ID {} not found", userId);
                return ResponseEntity.badRequest().body("User not found");
            }

            // Check stock availability
            if (!bookService.checkStockAvailability(bookId, quantity)) {
                logger.warn("Add to cart failed: Insufficient stock for book ID: {}, requested quantity: {}", bookId,
                        quantity);
                return ResponseEntity.badRequest().body("Insufficient stock");
            }

            // Check if item already in cart
            Cart existingItem = cartService.ifBookExistsInCart(userId, bookId);

            if (existingItem != null) {
                // Update quantity
                int newQuantity = existingItem.getQuantity() + quantity;
                logger.debug("Book ID: {} already in cart, updating quantity from {} to {}", bookId,
                        existingItem.getQuantity(), newQuantity);
                existingItem.setQuantity(newQuantity);
                cartService.updateCartQuantity(userId, bookId, existingItem.getQuantity());
            } else {
                // Add new item
                logger.debug("Adding new item to cart: Book ID: {}, User ID: {}, Quantity: {}", bookId, userId,
                        quantity);
                Cart cartItem = Cart.builder()
                        .user(user)
                        .book(book)
                        .quantity(quantity)
                        .addedDate(LocalDateTime.now())
                        .build();

                cartService.addToCart(userId, bookId, quantity);
            }

            logger.info("Book ID: {} successfully added to cart for user ID: {}", bookId, userId);
            return ResponseEntity.ok("Item added to cart");
        } catch (RuntimeException e) {
            logger.error("Error adding book ID: {} to cart for user ID: {}: {}", bookId, userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error adding book ID: {} to cart for user ID: {}: {}", bookId, userId,
                    e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{userId}/update")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updateCartItem(@PathVariable Long userId,
            @RequestParam Long bookId,
            @RequestParam Integer quantity) {
        logger.info("Request to update cart item: Book ID: {}, User ID: {}, New quantity: {}", bookId, userId,
                quantity);
        try {
            // Check if item exists in cart
            Cart cartItem = cartService.ifBookExistsInCart(userId, bookId);
            if (cartItem == null) {
                logger.warn("Update cart failed: Item with Book ID: {} not found in cart for User ID: {}", bookId,
                        userId);
                return ResponseEntity.badRequest().body("Item not found in cart");
            }

            // Check stock availability
            if (!bookService.checkStockAvailability(bookId, quantity)) {
                logger.warn("Update cart failed: Insufficient stock for Book ID: {}, requested quantity: {}", bookId,
                        quantity);
                return ResponseEntity.badRequest().body("Insufficient stock");
            }

            // Update quantity
            logger.debug("Updating quantity from {} to {} for Book ID: {} in User ID: {}'s cart",
                    cartItem.getQuantity(), quantity, bookId, userId);
            cartItem.setQuantity(quantity);
            cartService.updateCartQuantity(userId, bookId, quantity);

            logger.info("Cart item successfully updated for User ID: {}, Book ID: {}", userId, bookId);
            return ResponseEntity.ok("Cart updated");
        } catch (Exception e) {
            logger.error("Error updating cart item for User ID: {}, Book ID: {}: {}", userId, bookId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/remove")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> removeFromCart(@PathVariable Long userId,
            @RequestParam Long bookId) {
        logger.info("Request to remove Book ID: {} from cart for User ID: {}", bookId, userId);
        try {
            // Check if item exists in cart
            Cart cartItem = cartService.ifBookExistsInCart(userId, bookId);
            if (cartItem == null) {
                logger.warn("Remove from cart failed: Item with Book ID: {} not found in cart for User ID: {}", bookId,
                        userId);
                return ResponseEntity.badRequest().body("Item not found in cart");
            }

            // Remove item
            logger.debug("Removing Book ID: {} from User ID: {}'s cart", bookId, userId);
            cartService.removeFromCart(userId, bookId);

            logger.info("Book ID: {} successfully removed from User ID: {}'s cart", bookId, userId);
            return ResponseEntity.ok("Item removed from cart");
        } catch (Exception e) {
            logger.error("Error removing Book ID: {} from User ID: {}'s cart: {}", bookId, userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/clear")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        logger.info("Request to clear cart for User ID: {}", userId);
        try {
            UserInfo user = userInfoService.getUserById(userId);
            if (user == null) {
                logger.warn("Clear cart failed: User with ID {} not found", userId);
                return ResponseEntity.badRequest().body("User not found");
            }

            // Delete all cart items for user
            logger.debug("Clearing all items from User ID: {}'s cart", userId);
            cartService.clearCart(userId);

            logger.info("Cart successfully cleared for User ID: {}", userId);
            return ResponseEntity.ok("Cart cleared");
        } catch (Exception e) {
            logger.error("Error clearing cart for User ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
