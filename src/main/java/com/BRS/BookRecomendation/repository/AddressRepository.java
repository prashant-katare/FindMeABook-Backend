package com.BRS.BookRecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BRS.BookRecomendation.Entities.Address;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByUsername(String username);
}