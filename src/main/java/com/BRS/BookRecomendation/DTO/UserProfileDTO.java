package com.BRS.BookRecomendation.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String username;
    private String fullName;
    private String email;
    private int cartItemsCount;
    private int wishlistItemsCount;
}