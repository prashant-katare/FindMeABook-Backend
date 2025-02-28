package com.BRS.BookRecomendation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BRS.BookRecomendation.Entities.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    
    
}
