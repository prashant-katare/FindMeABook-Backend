package com.BRS.BookRecomendation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.BRS.BookRecomendation.DTO.Roles;
import com.BRS.BookRecomendation.DTO.SignUpData;
import com.BRS.BookRecomendation.Entities.Address;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.models.JwtRequest;
import com.BRS.BookRecomendation.models.JwtResponse;
import com.BRS.BookRecomendation.security.JwtHelper;
import com.BRS.BookRecomendation.service.UserInfoService;
import com.BRS.BookRecomendation.service.AddressService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private JwtHelper helper;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AddressService addressService;

    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    public ResponseEntity<String> addNewUser(@RequestBody SignUpData signUpData) {
        logger.info("Signup request received for email: {}", signUpData.getEmail());
        try {
            if (userInfoService.existsByUsername(signUpData.getEmail())) {
                logger.warn("Signup failed: Email {} is already in use", signUpData.getEmail());
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            logger.debug("Creating new user with email: {}", signUpData.getEmail());
            UserInfo userInfo = new UserInfo();

            userInfo.setFullName(signUpData.getFullName());
            userInfo.setUsername(signUpData.getEmail());
            userInfo.setPassword(signUpData.getPassword());
            userInfo.setRoles(Roles.ROLE_USER.toString());

            // Save user first
            userInfoService.addUser(userInfo);
            logger.info("New user registered successfully: {}", signUpData.getEmail());

            // Create and save default address
            logger.debug("Creating default address for user: {}", signUpData.getEmail());
            Address defaultAddress = new Address();
            defaultAddress.setStreet("Default Street");
            defaultAddress.setCity("Default City");
            defaultAddress.setState("Default State");
            defaultAddress.setZipCode("000000");
            defaultAddress.setPhoneNumber("0000000000");
            defaultAddress.setUser(userInfo);

            addressService.saveOrUpdateAddress(userInfo.getId(), defaultAddress);
            logger.info("Default address created for user: {}", signUpData.getEmail());

            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            logger.error("Error during user registration for email {}: {}", signUpData.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {
        logger.info("Login attempt for user: {}", request.getEmail());

        try {
            // The frontend will ask for Email and Password for login, therefore we all make
            // use on "email" variable instead of "username" in "JwtRequest" class.
            logger.debug("Authenticating user: {}", request.getEmail());
            this.doAuthenticate(request.getEmail(), request.getPassword());

            logger.debug("Loading user details for: {}", request.getEmail());
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

            logger.debug("Generating JWT token for user: {}", request.getEmail());
            String token = this.helper.generateToken(userDetails);

            JwtResponse response = JwtResponse.builder()
                    .jwtToken(token)
                    .username(userDetails.getUsername()).build();

            logger.info("User logged in successfully: {}", request.getEmail());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            logger.warn("Login failed for user: {} - Invalid credentials", request.getEmail());
            throw e;
        } catch (Exception e) {
            logger.error("Login error for user: {} - {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    private void doAuthenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            logger.debug("Attempting authentication for user: {}", email);
            manager.authenticate(authentication);
            logger.debug("Authentication successful for user: {}", email);
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed for user: {} - Invalid credentials", email);
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        } catch (Exception e) {
            logger.error("Authentication error for user: {} - {}", email, e.getMessage());
            throw e;
        }
    }

}
