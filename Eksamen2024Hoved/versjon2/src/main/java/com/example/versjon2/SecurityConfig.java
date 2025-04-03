package com.example.versjon2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ContentSecurityPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
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
                        // Definerer en Content Security Policy (CSP) for å kontrollere hvilke ressurser nettleseren kan laste inn.
                        // Dette reduserer risikoen for XSS-angrep.
                        .addHeaderWriter(new ContentSecurityPolicyHeaderWriter(
                                "default-src 'self'; " + // Tillater kun ressurser fra samme opprinnelse (domene, protokoll, port).
                                        "script-src 'self' 'unsafe-inline' https://cdn.example.com; " + // Tillater skript fra samme opprinnelse, inline skript (med forsiktighet), og et spesifikt CDN.
                                        "style-src 'self' https://fonts.googleapis.com; " + // Tillater stiler fra samme opprinnelse og Google Fonts.
                                        "img-src 'self' data:; " + // Tillater bilder fra samme opprinnelse og data-URIer (base64-kodede bilder).
                                        "font-src 'self' https://fonts.gstatic.com; " + // Tillater fonter fra samme opprinnelse og Google Fonts.
                                        "connect-src 'self' https://api.example.com; frame-src 'self'; " + // Tillater API-forespørsler til samme opprinnelse og et spesifikt API-domene, og iframes fra samme opprinnelse.
                                        "form-action 'self'; object-src 'none'; base-uri 'self'; " + // Begrenser hvor skjemaer kan sendes, tillater ikke plugins, og setter base-URI til samme opprinnelse.
                                        "require-trusted-types-for 'script'; report-to csp-endpoint;" // Krever Trusted Types for skript og sender CSP-rapporter til et endepunkt.
                        ))
                        // Legger til flere sikkerhets-headere.
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
}
