package com.BRS.BookRecomendation.repository;

import com.BRS.BookRecomendation.Entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {
    List<Wishlist> findByUserUsername(String username);

    boolean existsByUserUsernameAndBookId(String username, Long bookId);

    void deleteByUserUsernameAndBookId(String username, Long bookId);
}