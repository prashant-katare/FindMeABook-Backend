package com.BRS.BookRecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.BRS.BookRecomendation.Entities.UserInfo;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Long> {
    
    Optional<UserInfo> findByUsername(String username);
    
    boolean existsByUsername(String username);
}