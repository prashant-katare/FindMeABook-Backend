package com.BRS.BookRecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BRS.BookRecomendation.Entities.Address;
import com.BRS.BookRecomendation.Entities.UserInfo;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    
	Optional<Address> findByUser(UserInfo user);
    
	Optional<Address> findByUserId(Long userId);
}