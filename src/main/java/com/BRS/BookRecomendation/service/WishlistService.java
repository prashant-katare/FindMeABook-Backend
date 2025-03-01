package com.BRS.BookRecomendation.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.Entities.Wishlist;
import com.BRS.BookRecomendation.repository.BookRepository;
import com.BRS.BookRecomendation.repository.UserInfoRepository;
import com.BRS.BookRecomendation.repository.WishlistRepository;

@Service
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private BookRepository bookRepository;

    // Wishlist operations
    public void addToWishlist(Long userId, Long bookId) {
        logger.info("Attempting to add book ID: {} to wishlist for user: {}", bookId, userId);

        if (!wishlistRepository.existsByUserAndBook(userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")),
                bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("Book not found")))) {
            try {
                UserInfo user = userInfoRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                Book book = bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("Book not found"));

                Wishlist wishlist = Wishlist.builder()
                        .user(user)
                        .book(book)
                        .addedDate(LocalDateTime.now())
                        .build();

                wishlistRepository.save(wishlist);
                logger.info("Successfully added book ID: {} to wishlist for user: {}", bookId, userId);
            } catch (Exception e) {
                logger.error("Failed to add book to wishlist: {}", e.getMessage());
                throw e;
            }
        } else {
            logger.info("Book ID: {} already exists in wishlist for user: {}", bookId, userId);
        }
    }

    @Transactional
    public void removeFromWishlist(Long userId, Long bookId) {
        logger.info("Removing book ID: {} from wishlist for user: {}", bookId, userId);

        try {
            wishlistRepository.deleteByUserAndBook(userInfoRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found")),
                    bookRepository.findById(bookId)
                            .orElseThrow(() -> new RuntimeException("Book not found")));
            logger.info("Successfully removed book ID: {} from wishlist for user: {}", bookId, userId);
        } catch (Exception e) {
            logger.error("Error removing book from wishlist: {}", e.getMessage());
            throw e;
        }
    }

    public List<Wishlist> getUserWishlist(Long userId) {
        return wishlistRepository.findByUser(userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }

    public boolean ifBookExistsInWishlist(Long userId, Long bookId) {
        return wishlistRepository.existsByUserAndBook(userInfoRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")),
                bookRepository.findById(bookId)
                        .orElseThrow(() -> new RuntimeException("Book not found")));
    }

    public void clearWishlist(Long userId) {
        wishlistRepository.deleteAll(getUserWishlist(userId));
    }
}