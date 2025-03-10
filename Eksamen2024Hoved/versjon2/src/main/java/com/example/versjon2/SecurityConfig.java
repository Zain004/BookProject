package com.example.versjon2;

import org.springframework.http.HttpHeaders;

public class SecurityConfig {
    public static HttpHeaders createSecurityHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        // Content Security Policy (CSP) - **TILPASS DENNE TIL DINE BEHOV**
        headers.add("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' https://cdn.example.com; " +
                        "style-src 'self' https://fonts.googleapis.com; " +
                        "img-src 'self' data:; " +
                        "font-src 'self' https://fonts.gstatic.com; " +
                        "connect-src 'self' https://api.example.com; frame-src 'self'; " +
                        "form-action 'self'; object-src 'none'; base-uri 'self'; " +
                        "require-trusted-types-for 'script'; "+
                        "report-to csp-endpoint;"
        );
        headers.add("Report-To", "{ \"group\": \"csp-endpoint\", \"max_age\": 10886400, \"endpoints\": [ { \"url\": \"/csp-report-endpoint\" } ] }"); // Definer et endepunkt for å motta rapporter
        headers.add("Strict-Transport-Security", "max-age=31536000 ; includeSubDomains; preload"); // Inkluder preload for å bli lagt til i nettleserens preload-liste.
        headers.add("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");// Forhindrer caching av sensitivt innhold
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");  // Sender kun origin ved kryss-domene-forespørsler
        headers.add("Permissions-Policy", "geolocation=(), " +
                "microphone=(), camera=(), payment=(), usb=(),  gyroscope=(), magnetometer=(),  " +
                "picture-in-picture=(), display-capture=(), fullscreen=(self)"); //Begrens bruk av funksjoner.
        return headers;
    }
}
