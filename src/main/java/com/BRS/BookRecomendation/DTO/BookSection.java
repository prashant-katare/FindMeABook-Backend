package com.BRS.BookRecomendation.DTO;

import java.util.List;

import com.BRS.BookRecomendation.Entities.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BookSection {
	
	private String genre;
	List<Book> books;
	
}
