package com.BRS.BookRecomendation.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.Genre;
import com.BRS.BookRecomendation.Entities.Order;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.repository.BookRepository;
import com.BRS.BookRecomendation.repository.GenreRepository;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserInfoService userInfoService;

    // Book management
    public List<Book> getAllBooks() {
        logger.info("Admin retrieving all books");
        List<Book> books = bookRepository.findAll();
        logger.debug("Admin found {} books", books.size());
        return books;
    }

    public Book saveBook(Book book) {
        if (book.getId() == null) {
            logger.info("Admin creating new book: '{}' by {}", book.getTitle(), book.getAuthor());
        } else {
            logger.info("Admin updating book with ID: {}", book.getId());
        }

        Book savedBook = bookRepository.save(book);
        logger.debug("Admin saved book with ID: {}", savedBook.getId());
        return savedBook;
    }

    public void deleteBook(Long id) {
        logger.info("Admin deleting book with ID: {}", id);

        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            logger.debug("Admin successfully deleted book with ID: {}", id);
        } else {
            logger.warn("Admin attempted to delete non-existent book with ID: {}", id);
        }
    }

    // Genre management
    public List<Genre> getAllGenres() {
        logger.info("Admin retrieving all genres");
        List<Genre> genres = genreRepository.findAll();
        logger.debug("Admin found {} genres", genres.size());
        return genres;
    }

    public Genre saveGenre(Genre genre) {
        if (genre.getGenreId() == null) {
            logger.info("Admin creating new genre: {}", genre.getGenreTag());
        } else {
            logger.info("Admin updating genre with ID: {}", genre.getGenreId());
        }

        Genre savedGenre = genreRepository.save(genre);
        logger.debug("Admin saved genre with ID: {}", savedGenre.getGenreId());
        return savedGenre;
    }

    public void deleteGenre(Long id) {
        logger.info("Admin deleting genre with ID: {}", id);

        if (genreRepository.existsById(id)) {
            genreRepository.deleteById(id);
            logger.debug("Admin successfully deleted genre with ID: {}", id);
        } else {
            logger.warn("Admin attempted to delete non-existent genre with ID: {}", id);
        }
    }

    // Order management
    public List<Order> getAllOrders() {
        logger.info("Admin retrieving all orders");
        List<Order> orders = orderService.getAllOrders();
        logger.debug("Admin found {} orders", orders.size());
        return orders;
    }

    public Order updateOrderStatus(Long orderId, String status) {
        logger.info("Admin updating order status - Order ID: {}, New Status: {}", orderId, status);

        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            logger.debug("Admin successfully updated order status for Order ID: {}", orderId);
            return updatedOrder;
        } catch (Exception e) {
            logger.error("Admin failed to update order status for Order ID: {} - Error: {}",
                    orderId, e.getMessage());
            throw e;
        }
    }

    // User management
    public List<UserInfo> getAllUsers() {
        logger.info("Admin retrieving all users");
        List<UserInfo> users = userInfoService.getAllUsers();
        logger.debug("Admin found {} users", users.size());
        return users;
    }

    @Transactional
    public void deleteUser(Long userId) {
        logger.info("Admin deleting user with ID: {}", userId);

        try {
            userInfoService.deleteUser(userId);
            logger.debug("Admin successfully deleted user with ID: {}", userId);
        } catch (Exception e) {
            logger.error("Admin failed to delete user with ID: {} - Error: {}",
                    userId, e.getMessage());
            throw e;
        }
    }

}