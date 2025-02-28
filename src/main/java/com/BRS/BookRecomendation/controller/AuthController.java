package com.BRS.BookRecomendation.controller;

import java.util.List;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.BRS.BookRecomendation.DTO.Roles;
import com.BRS.BookRecomendation.DTO.SignUpData;
import com.BRS.BookRecomendation.Entities.Address;
import com.BRS.BookRecomendation.Entities.UserInfo;
import com.BRS.BookRecomendation.models.JwtRequest;
import com.BRS.BookRecomendation.models.JwtResponse;
import com.BRS.BookRecomendation.repository.AddressRepository;
import com.BRS.BookRecomendation.security.JwtHelper;
import com.BRS.BookRecomendation.service.UserInfoService;
import com.BRS.BookRecomendation.DTO.UserProfileDTO;
import com.BRS.BookRecomendation.service.UserBookService;
import com.BRS.BookRecomendation.DTO.PasswordUpdateDTO;


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
    private UserBookService userBookService;

    @Autowired
    private AddressRepository addressRepository;

    private Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/signup")
    public ResponseEntity<String> addNewUser(@RequestBody SignUpData signUpData) {

        if (userInfoService.existsByUsername(signUpData.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }

        UserInfo userInfo = new UserInfo();

        userInfo.setFullName(signUpData.getFullName());
        userInfo.setUsername(signUpData.getEmail());
        userInfo.setPassword(signUpData.getPassword());
        userInfo.setRoles(Roles.ROLE_USER.toString());

        // Save user first
        userInfoService.addUser(userInfo);
        
        // Create and save default address
        Address defaultAddress = new Address();
        defaultAddress.setStreet("Default Street");
        defaultAddress.setCity("Default City");
        defaultAddress.setState("Default State");
        defaultAddress.setZipCode("000000");
        defaultAddress.setPhoneNumber("0000000000");
        defaultAddress.setUsername(signUpData.getEmail());

        addressRepository.save(defaultAddress);

        return ResponseEntity.ok("User registered successfully!");
    }
    
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        // The frontend will ask for Email and Password for login, therefore we all make
        // use on "email" variable instead of "username" in "JwtRequest" class.
        this.doAuthenticate(request.getEmail(), request.getPassword());

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = this.helper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .username(userDetails.getUsername()).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void doAuthenticate(String email, String password) {

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public String exceptionHandler() {
        return "Credentials Invalid !!";
    }

    @GetMapping("/user/{username}/profile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        UserInfo user = userInfoService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        UserProfileDTO profileDTO = UserProfileDTO.builder()
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getUsername()) // since email is used as username
                .cartItemsCount(userBookService.getUserCart(username).size())
                .wishlistItemsCount(userBookService.getUserWishlist(username).size())
                .build();

        return ResponseEntity.ok(profileDTO);
    }

    @PutMapping("/user/{username}/profile")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updateUserProfile(@PathVariable String username, @RequestBody UserProfileDTO profileDTO) {
        UserInfo user = userInfoService.getUserByUsername(username);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Only allow updating fullName
        user.setFullName(profileDTO.getFullName());
        userInfoService.updateUser(user);

        return ResponseEntity.ok().build();
    }  

    @PutMapping("/user/{username}/password")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> updatePassword(@PathVariable String username, @RequestBody PasswordUpdateDTO passwordDTO) {
    	
        if (!userInfoService.updatePassword(username, passwordDTO.getCurrentPassword(), passwordDTO.getNewPassword())) {
        	
        	return ResponseEntity.badRequest().body("Current password is incorrect");
        	
        }
        return ResponseEntity.ok().build();
    }

    // Add or Update Address
    @PostMapping("/user/{username}/address")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> saveOrUpdateAddress(@PathVariable String username, @RequestBody Address address) {
        Address savedAddress = userBookService.saveOrUpdateAddress(username, address);
        return ResponseEntity.ok(savedAddress);
    }

    // Get Address
    @GetMapping("/user/{username}/address")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> getUserAddress(@PathVariable String username) {
        
        Optional<Address> address = userBookService.getUserAddress(username);
        
        if (address.isPresent()) {
            return ResponseEntity.ok(address.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Address not found for user: " + username);
        }
    }
    
}
