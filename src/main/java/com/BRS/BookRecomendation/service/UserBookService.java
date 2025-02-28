package com.BRS.BookRecomendation.service;

import com.BRS.BookRecomendation.Entities.*;
import com.BRS.BookRecomendation.controller.BookController;
import com.BRS.BookRecomendation.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Console;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserBookService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserInfoRepository userInfoRepository;
    
    @Autowired
    private AddressRepository addressRepository;
    
    private Logger logger = LoggerFactory.getLogger(UserBookService.class);

    // Wishlist operations
    public void addToWishlist(String username, Long bookId) {
        if (!wishlistRepository.existsByUserUsernameAndBookId(username, bookId)) {
        	
            UserInfo user = userInfoRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
        	
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .book(book)
                    .addedDate(LocalDateTime.now())
                    .build();
            
            wishlistRepository.save(wishlist);
        }
    }

    @Transactional // Ensures the delete operation runs within a transaction
    public void removeFromWishlist(String username, Long bookId) {
        
        wishlistRepository.deleteByUserUsernameAndBookId(username, bookId);
    }

    public List<Wishlist> getUserWishlist(String username) {
        return wishlistRepository.findByUserUsername(username);
    }

    // Cart operations
    public void addToCart(String username, Long bookId, Integer quantity) {
    	
        Cart existingItem = cartRepository.findByUserUsernameAndBookId(username, bookId);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartRepository.save(existingItem);
        } else {
            
            UserInfo user = userInfoRepository.findByUsername(username)
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
        }
    }

    public void updateCartQuantity(String username, Long bookId, Integer quantity) {
        Cart cart = cartRepository.findByUserUsernameAndBookId(username, bookId);
        if (cart != null) {
            cart.setQuantity(quantity);
            cartRepository.save(cart);
        }
    }
    
    @Transactional
    public void removeFromCart(String username, Long bookId) {
    		
        Cart cart = cartRepository.findByUserUsernameAndBookId(username, bookId);
        
        if (cart != null) {
            cartRepository.delete(cart);
        }
    }

    public List<Cart> getUserCart(String username) {
        return cartRepository.findByUserUsername(username);
    }

    // Additional helper method to check stock before adding to cart
    public boolean checkStockAvailability(Long bookId, Integer quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));
      
        return book.getStockQuantity() >= quantity;
    }
    
    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }
    
    // Add or Update Address
    public Address saveOrUpdateAddress(String username, Address address) {
        Address existingAddress = addressRepository.findByUsername(username).orElse(null);

        if (existingAddress != null) {
            // Update existing address
            existingAddress.setStreet(address.getStreet());
            existingAddress.setCity(address.getCity());
            existingAddress.setState(address.getState());
            existingAddress.setCountry(address.getCountry());
            existingAddress.setZipCode(address.getZipCode());
            existingAddress.setPhoneNumber(address.getPhoneNumber());
            return addressRepository.save(existingAddress);
        } else {
            // Create new address
            address.setUsername(username);
            return addressRepository.save(address);
        }
    }

    // Get User's Address
    public Optional<Address> getUserAddress(String username) {
        return addressRepository.findByUsername(username);
    }
    
    @Transactional
    public void removeAllBooks(String username) {
        // Find user by username
        UserInfo user = userInfoRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete all cart items for this user's ID
        cartRepository.deleteByUserId(user.getId());
    }
    
    
}