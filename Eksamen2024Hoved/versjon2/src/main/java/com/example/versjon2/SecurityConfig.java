package com.example.versjon2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean(name = "authenticationSecurityFilterChain")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF (ONLY for local testing - REMOVE IN PRODUCTION)
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll() // Allow H2-console access
                        .anyRequest().permitAll()  //  Allow all other requests - ADJUST IN PRODUCTION
                )
                .headers((headers) -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())  // Disable frame options to allow H2 console
                );

        return http.build();
    }

    @Bean // Bean er oppskrifter på hvordan lage ting, når den er laget brukes den til andre ting
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
