package com.BRS.BookRecomendation.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.BRS.BookRecomendation.Entities.Book;
import com.BRS.BookRecomendation.repository.BookRepository;

@Service
public class BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookRepository bookRepository;

    // Book retrieval methods
    public List<Book> getAllBooks() {
        logger.info("Retrieving all books");
        List<Book> books = bookRepository.findAll();
        logger.debug("Found {} books", books.size());
        return books;
    }

    public Optional<Book> getBookById(Long id) {
        logger.info("Retrieving book with ID: {}", id);
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            logger.debug("Found book: '{}' by {}", book.get().getTitle(), book.get().getAuthor());
        } else {
            logger.debug("Book with ID: {} not found", id);
        }
        return book;
    }

    public List<Book> getBooksByGenre(String genreTag) {
        logger.info("Retrieving books for genre tag: {}", genreTag);
        List<Book> books = bookRepository.findByGenreTag(genreTag);
        logger.debug("Found {} books for genre tag: {}", books.size(), genreTag);
        return books;
    }

    public List<Book> searchBooks(String query) {
        logger.info("Searching books with query: '{}'", query);
        List<Book> books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
        logger.debug("Found {} books matching query: '{}'", books.size(), query);
        return books;
    }

    // Book management methods
    public Book saveBook(Book book) {
        if (book.getId() == null) {
            logger.info("Creating new book: '{}' by {}", book.getTitle(), book.getAuthor());
        } else {
            logger.info("Updating book with ID: {}", book.getId());
        }

        Book savedBook = bookRepository.save(book);
        logger.debug("Book saved successfully with ID: {}", savedBook.getId());
        return savedBook;
    }

    public void deleteBook(Long id) {
        logger.info("Deleting book with ID: {}", id);

        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            logger.debug("Book with ID: {} deleted successfully", id);
        } else {
            logger.warn("Attempted to delete non-existent book with ID: {}", id);
        }
    }

    public Book updateBook(Book book) {
        logger.info("Updating book with ID: {}", book.getId());

        if (!bookRepository.existsById(book.getId())) {
            logger.warn("Attempted to update non-existent book with ID: {}", book.getId());
            return null;
        }

        Book updatedBook = bookRepository.save(book);
        logger.debug("Book with ID: {} updated successfully", updatedBook.getId());
        return updatedBook;
    }

    // Inventory management
    public boolean updateStock(Long bookId, int quantity) {
        logger.info("Updating stock for book ID: {} by {} units", bookId, quantity);

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            int currentStock = book.getStockQuantity();
            int newStock = currentStock + quantity;

            if (newStock >= 0) {
                book.setStockQuantity(newStock);
                bookRepository.save(book);
                logger.debug("Stock updated for book '{}' from {} to {}",
                        book.getTitle(), currentStock, newStock);
                return true;
            } else {
                logger.warn("Cannot update stock for book ID: {} - would result in negative stock ({} + {} = {})",
                        bookId, currentStock, quantity, newStock);
                return false;
            }
        } else {
            logger.error("Book with ID: {} not found for stock update", bookId);
            return false;
        }
    }

    public boolean checkStockAvailability(Long bookId, int requestedQuantity) {
        logger.debug("Checking stock availability for book ID: {} (requested: {})",
                bookId, requestedQuantity);

        Optional<Book> bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            boolean isAvailable = book.getStockQuantity() >= requestedQuantity;
            logger.trace("Book ID: {} stock check - available: {}, requested: {}, result: {}",
                    bookId, book.getStockQuantity(), requestedQuantity, isAvailable);
            return isAvailable;
        } else {
            logger.warn("Book with ID: {} not found for stock check", bookId);
            return false;
        }
    }

}