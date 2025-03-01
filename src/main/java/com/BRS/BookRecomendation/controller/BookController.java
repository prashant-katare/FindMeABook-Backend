package com.BRS.BookRecomendation.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import com.BRS.BookRecomendation.DTO.BookSection;
import com.BRS.BookRecomendation.Entities.Genre;
import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.service.BookService;
import com.BRS.BookRecomendation.service.GenreService;

@RestController
@RequestMapping("/book")
public class BookController {

	private Logger logger = LoggerFactory.getLogger(BookController.class);

	@Autowired
	private BookService bookService;

	@Autowired
	private GenreService genreService;

	@GetMapping("/hello")
	@PreAuthorize("hasAuthority('ROLE_USER')")
	public String sayHello() {
		logger.debug("Hello endpoint accessed");
		return "Hello, World!";
	}

	@GetMapping("/{bookId}")
	public ResponseEntity<Optional<Book>> getBook(@PathVariable Long bookId) {
		logger.info("Request to get book with ID: {}", bookId);
		try {
			Optional<Book> book = bookService.getBookById(bookId);
			if (book.isPresent()) {
				logger.info("Book found with ID: {}", bookId);
			} else {
				logger.warn("Book not found with ID: {}", bookId);
			}
			return ResponseEntity.ok(book);
		} catch (Exception e) {
			logger.error("Error retrieving book with ID {}: {}", bookId, e.getMessage());
			throw e;
		}
	}

	@GetMapping("/allBooks")
	public List<BookSection> getAllBooks() {
		logger.info("Request to get all books by genre sections");
		try {
			List<Genre> allGenreTags = genreService.getAllGenres();
			logger.debug("Retrieved {} genres", allGenreTags.size());

			ArrayList<BookSection> allBookSections = new ArrayList<>();
			BookSection tempBookSection;

			for (Genre genre : allGenreTags) {
				logger.debug("Processing books for genre: {}", genre.getGenreTag());
				tempBookSection = new BookSection();
				tempBookSection.setGenre(genre.getGenreTag());
				List<Book> books = bookService.getBooksByGenre(genre.getGenreTag());
				tempBookSection.setBooks(books);
				logger.debug("Added {} books for genre: {}", books.size(), genre.getGenreTag());
				allBookSections.add(tempBookSection);
			}

			logger.info("Returning {} book sections", allBookSections.size());
			return allBookSections;
		} catch (Exception e) {
			logger.error("Error retrieving all books: {}", e.getMessage());
			throw e;
		}
	}

	@GetMapping("/search")
	public ResponseEntity<List<Book>> searchBooks(@RequestParam String query) {
		logger.info("Search request received with query: '{}'", query);
		try {
			List<Book> books = bookService.searchBooks(query);
			logger.info("Search for '{}' returned {} results", query, books.size());
			return ResponseEntity.ok(books);
		} catch (Exception e) {
			logger.error("Error searching for books with query '{}': {}", query, e.getMessage());
			throw e;
		}
	}

}
