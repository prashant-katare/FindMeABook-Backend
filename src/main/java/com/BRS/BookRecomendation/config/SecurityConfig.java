package com.BRS.BookRecomendation.config;

import com.BRS.BookRecomendation.filters.JwtAuthenticationFilter;
import com.BRS.BookRecomendation.security.JwtAuthenticationEntryPoint;
import com.BRS.BookRecomendation.service.UserInfoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter authFilter;
    
    @Autowired
    private JwtAuthenticationEntryPoint point;

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserInfoService(); // Ensure UserInfoService implements UserDetailsService
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        	.cors(cors -> {})
            .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless APIs
            .authorizeHttpRequests(auth -> auth
            		.requestMatchers("*").permitAll()
            		.requestMatchers("/book/**").permitAll() // Allow all book routes
                .requestMatchers("/auth/login", "/auth/signup", "/book/allBooks").permitAll()
                .requestMatchers("/book/hello", "/book/user/**", "/auth/user/**").hasAuthority("ROLE_USER")
//                .requestMatchers("/book/admin/**").hasAuthority("ROLE_ADMIN")
//                .anyRequest().authenticated() // Protect all other endpoints
            )
            .sessionManagement(sess -> sess
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No sessions
            )
            .exceptionHandling(ex -> ex.authenticationEntryPoint(point))
            .authenticationProvider(authenticationProvider()) // Custom authentication provider
            .addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }             

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Password encoding
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
