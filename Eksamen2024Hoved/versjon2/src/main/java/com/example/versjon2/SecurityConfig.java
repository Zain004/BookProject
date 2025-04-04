package com.example.versjon2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean(name = "authenticationSecurityFilterChain") // Endre bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Konfigurerer sikkerhetsfilterkjeden for applikasjonen.
        http.authorizeHttpRequests((authz) -> authz
                        // Tillater alle forespørsler uten autentisering.
                        // *FORSIKTIG*: I en produksjonsapplikasjon, erstatt dette med spesifikke autorisasjonsregler.
                        .anyRequest().permitAll()
                )
                // Konfigurerer HTTP-headere for å forbedre sikkerheten.
                .headers((headers) -> headers
                        // Hindrer at applikasjonen blir innebygd i en iframe fra andre domener (Clickjacking beskyttelse).
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(XFrameOptionsHeaderWriter.XFrameOptionsMode.DENY))
                        .addHeaderWriter((request, response) -> {
                            // Konfigurerer Report-To header for CSP-rapportering.
                            response.setHeader("Report-To", "{ \"group\": \"csp-endpoint\", \"max_age\": 10886400, \"endpoints\": [ { \"url\": \"/csp-report-endpoint\" } ] }");
                            // Aktiverer HTTP Strict Transport Security (HSTS) for å tvinge HTTPS-tilkoblinger.
                            response.setHeader("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains; preload");
                            // Deaktiverer cachelagring av sensitive data.
                            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                            // Setter Referrer-Policy for å kontrollere hvor mye informasjon som sendes i Referer-headeren.
                            response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
                            // Setter Permissions-Policy (tidligere Feature-Policy) for å kontrollere tilgangen til nettleserfunksjoner.
                            response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=(), payment=(), usb=(), gyroscope=(), magnetometer=(), picture-in-picture=(), display-capture=(), fullscreen=(self)");
                        })
                );

        // Bygger og returnerer sikkerhetsfilterkjeden.
        return http.build();
    }

    @Bean // Bean er oppskrifter på hvordan lage ting, når den er laget brukes den til andre ting
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
