package com.BRS.BookRecomendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.BRS.BookRecomendation.Entities.Book;

public interface BookRepository  extends JpaRepository<Book, Long> {
    
    List<Book> findByGenreId(Long genreId);
    
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    
}
