package com.BRS.BookRecomendation.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.BRS.BookRecomendation.DTO.PasswordUpdateDTO;
import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.Genre;
import com.BRS.BookRecomendation.Entities.Order;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.service.OrderService;
import com.BRS.BookRecomendation.service.UserInfoService;
import com.BRS.BookRecomendation.service.WishlistService;
import com.BRS.BookRecomendation.service.CartService;
import com.BRS.BookRecomendation.service.AddressService;
import com.BRS.BookRecomendation.service.BookService;
import com.BRS.BookRecomendation.service.GenreService;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private BookService bookService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;
    
    
    @PutMapping("{userId}/updatePassword")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody PasswordUpdateDTO passwordDTO) {
        logger.info("Request to update password for User ID: {}", userId);
        try {
            if (!userInfoService.updatePassword(userId, passwordDTO.getCurrentPassword(),
                    passwordDTO.getNewPassword())) {
                logger.warn("Password update failed for User ID: {} - Current password is incorrect", userId);
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }

            logger.info("Password successfully updated for User ID: {}", userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating password for User ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Password update failed: " + e.getMessage());
        }
    }

    // Book Management
    @PostMapping("/books")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        logger.info("Admin request to add new book: {}", book.getTitle());
        try {
            Book savedBook = bookService.saveBook(book);
            logger.info("Book added successfully: {}", savedBook.getId());
            return ResponseEntity.ok(savedBook);
        } catch (Exception e) {
            logger.error("Failed to add book '{}': {}", book.getTitle(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/books/{bookId}")
    public ResponseEntity<?> updateBook(@PathVariable Long bookId, @RequestBody Book book) {
        logger.info("Admin request to update book with ID: {}", bookId);
        try {
            if (!bookService.getBookById(bookId).isPresent()) {
                logger.warn("Book update failed: Book with ID {} not found", bookId);
                return ResponseEntity.notFound().build();
            }

            book.setId(bookId); // Ensure ID is set correctly
            Book updatedBook = bookService.saveBook(book);
            logger.info("Book with ID {} updated successfully", bookId);
            return ResponseEntity.ok(updatedBook);
        } catch (Exception e) {
            logger.error("Error updating book with ID {}: {}", bookId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        logger.info("Admin request to delete book with ID: {}", bookId);
        try {
            if (!bookService.getBookById(bookId).isPresent()) {
                logger.warn("Book deletion failed: Book with ID {} not found", bookId);
                return ResponseEntity.notFound().build();
            }

            bookService.deleteBook(bookId);
            logger.info("Book with ID {} deleted successfully", bookId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting book with ID {}: {}", bookId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Genre Management
    @GetMapping("/genres")
    public ResponseEntity<List<Genre>> getAllGenres() {
        logger.info("Admin request to get all genres");
        try {
            List<Genre> genres = genreService.getAllGenres();
            logger.info("Returning {} genres", genres.size());
            return ResponseEntity.ok(genres);
        } catch (Exception e) {
            logger.error("Error retrieving genres: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/genres")
    public ResponseEntity<Genre> addGenre(@RequestBody Genre genre) {
        logger.info("Admin request to add new genre: {}", genre.getGenreTag());
        try {
            Genre savedGenre = genreService.saveGenre(genre);
            logger.info("Genre added successfully: {}", savedGenre.getGenreId());
            return ResponseEntity.ok(savedGenre);
        } catch (Exception e) {
            logger.error("Failed to add genre '{}': {}", genre.getGenreTag(), e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/genres/{genreId}")
    public ResponseEntity<?> deleteGenre(@PathVariable Long genreId) {
        logger.info("Admin request to delete genre with ID: {}", genreId);
        try {
            if (!genreService.getGenreById(genreId).isPresent()) {
                logger.warn("Genre deletion failed: Genre with ID {} not found", genreId);
                return ResponseEntity.notFound().build();
            }

            genreService.deleteGenre(genreId);
            logger.info("Genre with ID {} deleted successfully", genreId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting genre with ID {}: {}", genreId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        logger.info("Admin request to get all orders");
        try {
            List<Order> orders = orderService.getAllOrders();
            logger.info("Returning {} orders", orders.size());
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            logger.error("Error retrieving orders: {}", e.getMessage());
            throw e;
        }
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        logger.info("Admin request to update order status: ID={}, newStatus={}", orderId, status);
        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            logger.info("Order status updated successfully: ID={}, status={}", orderId, status);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            logger.error("Order status update failed: ID={}, error={}", orderId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        logger.info("Admin request to get all users");
        try {
            List<UserInfo> users = userInfoService.getAllUsers();
            logger.info("Returning {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving users: {}", e.getMessage());
            throw e;
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        logger.info("Admin request to delete user with ID: {}", userId);
        try {
            if (userInfoService.getUserById(userId) == null) {
                logger.warn("User deletion failed: User with ID {} not found", userId);
                return ResponseEntity.badRequest().body("User not found");
            }

            logger.debug("Clearing wishlist for user ID: {}", userId);
            wishlistService.clearWishlist(userId);

            logger.debug("Clearing cart for user ID: {}", userId);
            cartService.clearCart(userId);

            logger.debug("Cancelling all orders for user ID: {}", userId);
            orderService.cancelAllOrders(userId);

            logger.debug("Deleting address for user ID: {}", userId);
            addressService.deleteAddress(userId);

            logger.debug("Deleting user account for ID: {}", userId);
            userInfoService.deleteUser(userId);

            logger.info("User with ID {} deleted successfully", userId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("User deletion failed: ID={}, error={}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}