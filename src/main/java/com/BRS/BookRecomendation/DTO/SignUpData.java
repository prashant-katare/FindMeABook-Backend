package com.BRS.BookRecomendation.DTO;


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
public class SignUpData {
	
	private String fullName;
	private String email;
	private String password;
	
}