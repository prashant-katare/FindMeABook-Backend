package com.BRS.BookRecomendation.repository;

import com.BRS.BookRecomendation.Entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserUsername(String username);

    Cart findByUserUsernameAndBookId(String username, Long bookId);

    boolean existsByUserUsernameAndBookId(String username, Long bookId);
    
    void deleteByUserId(Long userId);
}