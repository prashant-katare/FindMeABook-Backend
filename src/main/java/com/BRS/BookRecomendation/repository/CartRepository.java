package com.BRS.BookRecomendation.repository;

import com.BRS.BookRecomendation.Entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.BRS.BookRecomendation.Entities.UserInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByUser(UserInfo user);

    @Query("SELECT c FROM Cart c WHERE c.user.id = :userId")
    List<Cart> findByUserId(@Param("userId") Long userId);

    Cart findByUserIdAndBookId(Long userId, Long bookId);

    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Cart c WHERE c.user.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
