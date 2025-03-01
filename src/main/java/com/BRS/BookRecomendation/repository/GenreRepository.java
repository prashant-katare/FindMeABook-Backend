package com.BRS.BookRecomendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BRS.BookRecomendation.Entities.Genre;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findByGenreId(Long genreId);

    boolean existsByGenreTag(String genreTag);
}
