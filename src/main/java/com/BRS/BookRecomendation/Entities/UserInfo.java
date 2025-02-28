package com.BRS.BookRecomendation.Entities;

import lombok.*;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String fullName;
    private String password;
    private String roles;
}


