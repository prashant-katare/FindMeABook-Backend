package com.BRS.BookRecomendation.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.BRS.BookRecomendation.DTO.WishlistItemDTO;
import com.BRS.BookRecomendation.Entities.Wishlist;
import com.BRS.BookRecomendation.service.BookService;
import com.BRS.BookRecomendation.service.UserInfoService;
import com.BRS.BookRecomendation.service.WishlistService;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {

    private final Logger logger = LoggerFactory.getLogger(WishlistController.class);

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private BookService bookService;

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List<WishlistItemDTO>> getUserWishlist(@PathVariable Long userId) {
        logger.info("Request to get wishlist for User ID: {}", userId);
        try {
            List<Wishlist> wishlistItems = wishlistService.getUserWishlist(userId);
            logger.debug("Retrieved {} wishlist items for User ID: {}", wishlistItems.size(), userId);

            List<WishlistItemDTO> wishlistItemDTOs = wishlistItems.stream()
                    .map(item -> WishlistItemDTO.builder()
                            .bookId(item.getBook().getId())
                            .title(item.getBook().getTitle())
                            .author(item.getBook().getAuthor())
                            .imageUrl(item.getBook().getImageUrl())
                            .price(item.getBook().getPrice())
                            .addedDate(item.getAddedDate())
                            .build())
                    .collect(Collectors.toList());

            logger.info("Returning {} wishlist items for User ID: {}", wishlistItemDTOs.size(), userId);
            return ResponseEntity.ok(wishlistItemDTOs);
        } catch (Exception e) {
            logger.error("Error retrieving wishlist for User ID: {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{userId}/add")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> addToWishlist(@PathVariable Long userId,
            @RequestParam Long bookId) {
        logger.info("Request to add Book ID: {} to wishlist for User ID: {}", bookId, userId);
        try {
            // Check if book exists
            if (!bookService.getBookById(bookId).isPresent()) {
                logger.warn("Add to wishlist failed: Book ID: {} not found", bookId);
                return ResponseEntity.badRequest().body("Book not found");
            }

            // Check if user exists
            if (userInfoService.getUserById(userId) == null) {
                logger.warn("Add to wishlist failed: User ID: {} not found", userId);
                return ResponseEntity.badRequest().body("User not found");
            }

            // Check if item already in wishlist
            boolean exists = wishlistService.ifBookExistsInWishlist(userId, bookId);

            if (exists) {
                logger.warn("Add to wishlist failed: Book ID: {} already in wishlist for User ID: {}", bookId, userId);
                return ResponseEntity.badRequest().body("Item already in wishlist");
            }

            logger.debug("Adding Book ID: {} to wishlist for User ID: {}", bookId, userId);
            wishlistService.addToWishlist(userId, bookId);

            logger.info("Book ID: {} successfully added to wishlist for User ID: {}", bookId, userId);
            return ResponseEntity.ok("Item added to wishlist");
        } catch (Exception e) {
            logger.error("Error adding Book ID: {} to wishlist for User ID: {}: {}", bookId, userId, e.getMessage());
            return ResponseEntity.badRequest().body("Failed to add to wishlist: " + e.getMessage());
        }
    }

    @DeleteMapping("/{userId}/remove")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long userId,
            @RequestParam Long bookId) {
        logger.info("Request to remove Book ID: {} from wishlist for User ID: {}", bookId, userId);
        try {
            // Check if item exists in wishlist
            boolean exists = wishlistService.ifBookExistsInWishlist(userId, bookId);
            if (!exists) {
                logger.warn("Remove from wishlist failed: Book ID: {} not found in wishlist for User ID: {}", bookId,
                        userId);
                return ResponseEntity.badRequest().body("Item not found in wishlist");
            }

            // Remove item
            logger.debug("Removing Book ID: {} from wishlist for User ID: {}", bookId, userId);
            wishlistService.removeFromWishlist(userId, bookId);

            logger.info("Book ID: {} successfully removed from wishlist for User ID: {}", bookId, userId);
            return ResponseEntity.ok("Item removed from wishlist");
        } catch (Exception e) {
            logger.error("Error removing Book ID: {} from wishlist for User ID: {}: {}", bookId, userId,
                    e.getMessage());
            return ResponseEntity.badRequest().body("Failed to remove from wishlist: " + e.getMessage());
        }
    }

    @PostMapping("/{userId}/move-to-cart")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> moveToCart(@PathVariable Long userId,
            @RequestParam Long bookId) {
        logger.info("Request to move Book ID: {} from wishlist to cart for User ID: {}", bookId, userId);
        try {
            // Check if item exists in wishlist
            boolean exists = wishlistService.ifBookExistsInWishlist(userId, bookId);
            if (!exists) {
                logger.warn("Move to cart failed: Book ID: {} not found in wishlist for User ID: {}", bookId, userId);
                return ResponseEntity.badRequest().body("Item not found in wishlist");
            }

            // Add to cart
            logger.debug("Adding Book ID: {} to cart for User ID: {}", bookId, userId);
            ResponseEntity<?> addToCartResponse = new CartController().addToCart(userId, bookId, 1);

            if (addToCartResponse.getStatusCode().is2xxSuccessful()) {
                // Remove from wishlist
                logger.debug("Removing Book ID: {} from wishlist for User ID: {}", bookId, userId);
                wishlistService.removeFromWishlist(userId, bookId);
                logger.info("Book ID: {} successfully moved from wishlist to cart for User ID: {}", bookId, userId);
                return ResponseEntity.ok("Item moved to cart");
            } else {
                logger.warn("Failed to add Book ID: {} to cart for User ID: {}", bookId, userId);
                return addToCartResponse;
            }
        } catch (Exception e) {
            logger.error("Error moving Book ID: {} from wishlist to cart for User ID: {}: {}", bookId, userId,
                    e.getMessage());
            return ResponseEntity.badRequest().body("Failed to move to cart: " + e.getMessage());
        }
    }
}