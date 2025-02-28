package com.BRS.BookRecomendation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.BRS.BookRecomendation.DTO.UserInfoDetails;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.repository.UserInfoRepository;

import java.util.Optional;

@Service
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserInfo> userDetail = repository.findByUsername(username); // Assuming 'email' is used as username

        // Converting UserInfo to UserDetails
        return userDetail.map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public String addUser(UserInfo userInfo) {
        // Encode password before saving the user
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "User Added Successfully";
    }

    public boolean existsByUsername(String username) {

        return repository.existsByUsername(username);

    }

    public UserInfo getUserByUsername(String username) {
        return repository.findByUsername(username).orElse(null);
    }

    public void updateUser(UserInfo user) {
        repository.save(user);
    }

    public boolean updatePassword(String username, String currentPassword, String newPassword) {
        UserInfo user = getUserByUsername(username);
        if (user != null && encoder.matches(currentPassword, user.getPassword())) {
            user.setPassword(encoder.encode(newPassword));
            repository.save(user);
            return true;
        }
        return false;
    }
}
