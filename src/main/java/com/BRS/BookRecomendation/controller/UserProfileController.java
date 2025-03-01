package com.BRS.BookRecomendation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.BRS.BookRecomendation.DTO.UserProfileDTO;
import com.BRS.BookRecomendation.Entities.Address;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.service.UserInfoService;
import com.BRS.BookRecomendation.DTO.PasswordUpdateDTO;
import com.BRS.BookRecomendation.service.AddressService;

import java.util.Optional;

@RestController
@RequestMapping("/profile")
public class UserProfileController {

    private final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AddressService addressService;

    @PutMapping("{userId}/updatePassword")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody PasswordUpdateDTO passwordDTO) {
        logger.info("Request to update password for User ID: {}", userId);
        try {
            if (!userInfoService.updatePassword(userId, passwordDTO.getCurrentPassword(),
                    passwordDTO.getNewPassword())) {
                logger.warn("Password update failed for User ID: {} - Current password is incorrect", userId);
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }

            logger.info("Password successfully updated for User ID: {}", userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating password for User ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Password update failed: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable Long userId) {
        logger.info("Request to get profile for User ID: {}", userId);
        try {
            UserInfo user = userInfoService.getUserById(userId);
            if (user == null) {
                logger.warn("Profile not found for User ID: {}", userId);
                return ResponseEntity.notFound().build();
            }

            logger.debug("Building profile DTO for User ID: {}", userId);
            UserProfileDTO profileDTO = UserProfileDTO.builder()
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getUsername()) // since email is used as username
                    .build();

            logger.info("Successfully retrieved profile for User ID: {}", userId);
            return ResponseEntity.ok(profileDTO);
        } catch (Exception e) {
            logger.error("Error retrieving profile for User ID: {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{userId}/update")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updateUserProfile(@PathVariable Long userId, @RequestBody UserProfileDTO profileDTO) {
        logger.info("Request to update profile for User ID: {}", userId);
        try {
            UserInfo user = userInfoService.getUserById(userId);

            if (user == null) {
                logger.warn("Profile update failed: User ID: {} not found", userId);
                return ResponseEntity.notFound().build();
            }

            logger.debug("Updating fullName from '{}' to '{}' for User ID: {}",
                    user.getFullName(), profileDTO.getFullName(), userId);

            // Only allow updating fullName
            user.setFullName(profileDTO.getFullName());
            userInfoService.updateUser(user);

            logger.info("Profile successfully updated for User ID: {}", userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error updating profile for User ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Profile update failed: " + e.getMessage());
        }
    }

    @GetMapping("/{userId}/getUserAddress")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserAddress(@PathVariable Long userId) {
        logger.info("Request to get address for User ID: {}", userId);
        try {
            Optional<Address> address = addressService.getUserAddress(userId);

            if (address.isPresent()) {
                logger.info("Address found for User ID: {}", userId);
                return ResponseEntity.ok(address.get());
            } else {
                logger.warn("Address not found for User ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving address for User ID: {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{userId}/saveOrUpdateAddress")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> saveOrUpdateAddress(@PathVariable Long userId, @RequestBody Address address) {
        logger.info("Request to save/update address for User ID: {}", userId);
        try {
            logger.debug("Processing address update for User ID: {}", userId);
            Address savedAddress = addressService.saveOrUpdateAddress(userId, address);
            logger.info("Address successfully saved/updated for User ID: {}", userId);
            return ResponseEntity.ok(savedAddress);
        } catch (Exception e) {
            logger.error("Error saving/updating address for User ID: {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body("Address update failed: " + e.getMessage());
        }
    }
}