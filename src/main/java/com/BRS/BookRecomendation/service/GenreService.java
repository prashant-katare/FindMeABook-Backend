package com.BRS.BookRecomendation.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BRS.BookRecomendation.Entities.Genre;
import com.BRS.BookRecomendation.repository.GenreRepository;

@Service
public class GenreService {

    private static final Logger logger = LoggerFactory.getLogger(GenreService.class);

    @Autowired
    private GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        logger.debug("Retrieving all genres");
        List<Genre> genres = genreRepository.findAll();
        logger.debug("Retrieved {} genres", genres.size());
        return genres;
    }

    public Optional<Genre> getGenreById(Long genreId) {
        logger.debug("Retrieving genre with ID: {}", genreId);
        Optional<Genre> genre = genreRepository.findById(genreId);
        if (genre.isPresent()) {
            logger.debug("Genre found with ID: {}", genreId);
        } else {
            logger.debug("No genre found with ID: {}", genreId);
        }
        return genre;
    }

    public Genre saveGenre(Genre genre) {
        if (genre.getGenreId() == null) {
            logger.info("Admin creating new genre: {}", genre.getGenreTag());
        } else {
            logger.info("Admin updating genre with ID: {}, new tag: {}",
                    genre.getGenreId(), genre.getGenreTag());
        }

        Genre savedGenre = genreRepository.save(genre);
        logger.info("Genre saved successfully with ID: {}", savedGenre.getGenreId());
        return savedGenre;
    }

    @Transactional
    public void deleteGenre(Long genreId) {
        logger.info("Admin attempting to delete genre with ID: {}", genreId);

        if (!genreRepository.existsById(genreId)) {
            logger.warn("Delete operation failed - genre with ID: {} not found", genreId);
            throw new RuntimeException("Genre not found with ID: " + genreId);
        }

        // You might want to add additional checks here if genres have relationships
        // For example, check if books are using this genre before deletion

        genreRepository.deleteById(genreId);
        logger.info("Genre with ID: {} successfully deleted", genreId);
    }

    public boolean existsByGenreTag(String genreTag) {
        logger.debug("Checking if genre tag exists: {}", genreTag);
        boolean exists = genreRepository.existsByGenreTag(genreTag);
        logger.debug("Genre tag '{}' exists: {}", genreTag, exists);
        return exists;
    }
}