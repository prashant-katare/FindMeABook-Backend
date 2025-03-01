package com.BRS.BookRecomendation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.BRS.BookRecomendation.Entities.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByGenreTag(String genreTag);

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

}
