package com.BRS.BookRecomendation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BRS.BookRecomendation.DTO.UserInfoDetails;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.repository.CartRepository;
import com.BRS.BookRecomendation.repository.UserInfoRepository;
import com.BRS.BookRecomendation.repository.WishlistRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserInfoService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserInfoService.class);

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username: {}", username);
        Optional<UserInfo> userDetail = userInfoRepository.findByUsername(username);

        // Converting UserInfo to UserDetails
        return userDetail.map(userInfo -> {
            logger.debug("User found: {}", username);
            return new UserInfoDetails(userInfo);
        }).orElseThrow(() -> {
            logger.warn("Failed login attempt - user not found: {}", username);
            return new UsernameNotFoundException("User not found: " + username);
        });
    }

    public String addUser(UserInfo userInfo) {
        logger.info("Adding new user with username: {}", userInfo.getUsername());
        // Encode password before saving the user
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        userInfoRepository.save(userInfo);
        logger.info("User successfully added: {}", userInfo.getUsername());
        return "User Added Successfully";
    }

    public boolean existsByUsername(String username) {
        logger.debug("Checking if username exists: {}", username);
        boolean exists = userInfoRepository.existsByUsername(username);
        logger.debug("Username {} exists: {}", username, exists);
        return exists;
    }

    public UserInfo getUserByUsername(String username) {
        logger.debug("Retrieving user by username: {}", username);
        UserInfo user = userInfoRepository.findByUsername(username).orElse(null);
        if (user == null) {
            logger.debug("No user found with username: {}", username);
        } else {
            logger.debug("User found with username: {}", username);
        }
        return user;
    }

    public UserInfo getUserById(Long userId) {
        logger.debug("Retrieving user by ID: {}", userId);
        UserInfo user = userInfoRepository.findById(userId).orElse(null);
        if (user == null) {
            logger.debug("No user found with ID: {}", userId);
        } else {
            logger.debug("User found with ID: {}", userId);
        }
        return user;
    }

    public void updateUser(UserInfo user) {
        logger.info("Updating user with ID: {}", user.getId());
        userInfoRepository.save(user);
        logger.info("User with ID: {} successfully updated", user.getId());
    }

    public List<UserInfo> getAllUsers() {
        logger.debug("Retrieving all users");
        List<UserInfo> users = userInfoRepository.findAll();
        logger.debug("Retrieved {} users", users.size());
        return users;
    }

    @Transactional
    public void deleteUser(Long userId) {
        logger.info("Attempting to delete user with ID: {}", userId);

        userInfoRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User with ID {} not found", userId);
                    throw new UsernameNotFoundException("User with ID " + userId + " not found");
                });

        logger.info("Deleting cart items for user ID: {}", userId);
        int cartItemsDeleted = cartRepository.deleteByUserId(userId);
        logger.debug("Deleted {} cart items for user ID: {}", cartItemsDeleted, userId);

        logger.info("Deleting wishlist items for user ID: {}", userId);
        int wishlistItemsDeleted = wishlistRepository.deleteByUserId(userId);
        logger.debug("Deleted {} wishlist items for user ID: {}", wishlistItemsDeleted, userId);

        logger.info("Deleting user with ID: {}", userId);
        userInfoRepository.deleteById(userId);
        logger.info("User with ID: {} successfully deleted", userId);
    }

    public boolean updatePassword(Long userId, String currentPassword, String newPassword) {
        logger.info("Password update requested for user ID: {}", userId);
        UserInfo user = userInfoRepository.findById(userId).orElse(null);

        if (user == null) {
            logger.warn("Password update failed - user with ID: {} not found", userId);
            return false;
        }

        // Verify current password - don't log passwords!
        if (!encoder.matches(currentPassword, user.getPassword())) {
            logger.warn("Password update failed - incorrect current password for user ID: {}", userId);
            return false;
        }

        // Update password - don't log passwords!
        user.setPassword(encoder.encode(newPassword));
        userInfoRepository.save(user);
        logger.info("Password successfully updated for user ID: {}", userId);

        return true;
    }
}
