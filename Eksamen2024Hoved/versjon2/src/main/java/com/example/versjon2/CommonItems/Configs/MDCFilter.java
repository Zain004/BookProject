package com.example.versjon2.CommonItems.Configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MDCFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString(); // Generer request ID her, eller hent fra header
        MDC.put("requestId", requestId);
        try { // sender forespørselen videre ned i filterkjeden enten til neste filter eller
            filterChain.doFilter(request, response); // til controllern
        } finally {
            MDC.clear(); // Fjern alt fra mdc, for å gjenbruke tråder og at ingen data lekker
        }
    }
}
