package com.BRS.BookRecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.BRS.BookRecomendation.Entities.UserInfo;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
    Optional<UserInfo> findByUsername(String username);
    
    boolean existsByUsername(String username);
    
    @Query("SELECT u.id FROM UserInfo u WHERE u.username = :username")
    Optional<Long> findIdByUsername(@Param("username") String username);
    
    @Query("SELECT u FROM UserInfo u WHERE u.id = :id")
    UserInfo getUserById(@Param("id") Long id);
    
}