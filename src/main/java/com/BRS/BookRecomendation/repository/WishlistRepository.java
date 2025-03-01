package com.BRS.BookRecomendation.repository;

import com.BRS.BookRecomendation.Entities.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.Entities.Book;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByUser(UserInfo user);

    boolean existsByUserAndBook(UserInfo user, Book book);

    void deleteByUserAndBook(UserInfo user, Book book);

    @Transactional
    int deleteByUser(UserInfo user);

    @Query("SELECT w FROM Wishlist w WHERE w.user.id = :userId")
    List<Wishlist> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Wishlist w WHERE w.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
