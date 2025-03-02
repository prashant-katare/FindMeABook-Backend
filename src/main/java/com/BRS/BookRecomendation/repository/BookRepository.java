package com.BRS.BookRecomendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.Entities.Order;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByGenreTag(String genreTag);

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Book getBookById(@Param("id") Long id);

}
