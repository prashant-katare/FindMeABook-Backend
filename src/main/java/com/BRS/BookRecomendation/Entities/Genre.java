package com.BRS.BookRecomendation.Entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "genres")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Genre {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long genreId;
    
    private String genreTag;
	
}	
