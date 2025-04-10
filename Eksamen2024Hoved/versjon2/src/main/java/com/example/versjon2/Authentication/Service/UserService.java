package com.example.versjon2.Authentication.Service;

import com.example.versjon2.Authentication.Repository.UserRepository;
import com.example.versjon2.Authentication.UserEntity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    /**
     *
     * @param username
     * @param password
     * @param request
     * @return
     */
    @Transactional
    public Optional<User> login(String username, String password, HttpServletRequest request) {
        logger.info("Attempting login for user: {}", username);
        // Input validation
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            logger.warn("Login attempt with invalid username or password.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password or username");
        }
        Optional<User> optionalUser = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> {
                    logger.warn("Login failed: USer {} not found", username);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User dont exist");
                });
        User user = new User();
        if(optionalUser.isPresent()) {
            user = optionalUser.get();
        }
        if(!passwordEncoder.matches(password, optionalUser.get().getPasswordHash())) {
            logger.warn("Login failed: Invalid password for user: {}", username);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }
        HttpSession existingSession = request.getSession(false);
        if(existingSession != null) {
            logger.info("Invalidating existing session for user: {}", username);
            clearSessionAttributes(existingSession); // ugyldiggjør
        }
        return authenticateUser(request, user);
    }
    /**
     *
     * @param request
     * @param user
     * @return
     */
    private Optional<User> authenticateUser(HttpServletRequest request, User user) {
        logger.info("Authenticating session on user request");
        HttpSession session = request.getSession(true);
        // Generer secure session id
        String secureSessionId = generateSecureSessionID();
        // Lagre bruker-ID, brukernavn og en tidsstempel for å sjekke om session er gammel
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("user", user);
        session.setAttribute("lastActivity", Instant.now());
        session.setAttribute("secureSessionId", secureSessionId);
        // Session time is set in APPLICATION.PROPERTIES
        logger.info("User {} successfully logged in with session ID: {}," +
                "sessionID: {}", user.getUsername(), session.getId(), secureSessionId);
        return Optional.of(user);
    }
    /**
     *
     * @return
     */
    private String generateSecureSessionID() {
        return UUID.randomUUID().toString(); // generer en unik universell ID
    }
    /**
     *
     * @param request
     */
    @Transactional
    public void logout(HttpServletRequest request) {
        logger.info("Retrieved request to logout.");
        HttpSession session = request.getSession(false); // den er satt til false for å unngå å opprette en ny sesjon
        if(session == null) {
            logger.warn("Logout attempt without an existing session.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You're not logged in");
        }
        String sessionId = session.getId();
        logger.info("Attempting logout for session ID: {}", sessionId);
        clearSessionAttributes(session);
        logger.info("User with session ID: {} successfully logged out and session invalidated.", sessionId);
    }
    /**
     *
     * @param session
     */
    private void clearSessionAttributes(HttpSession session) {
        Enumeration<String> attributeNames = session.getAttributeNames();
        // Fjern alle attributer
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            session.removeAttribute(attributeName);
            logger.debug("Removed session attribute : {}", attributeName);
        }
        logger.debug("All session attributes are removed.");
    }
    /**
     *
     * @param request
     * @return
     */
    // autentiserer brukers login status
    public boolean isAuthenticated(HttpServletRequest request) {
        logger.info("Checking login status for user {}", request.getSession().getId());
        return Optional.ofNullable(request.getSession(false))
                .map(this::isAuthenticated) // peker til sammem metode i klasse
                .orElseGet(() -> {
                    logger.debug("Authentication check: no active session found.");
                    return false;
                });
    }

    /**
     *
     * @param session
     * @return
     */
    private boolean isAuthenticated(HttpSession session) {
        if(isSessionExpired(session)) {
            logger.debug("Authentication check: Session is expired");
            return false;
        }
        Object userId = session.getAttribute("userId");
        Optional.ofNullable(userId)
                .orElseGet(() -> {
                    logger.warn("Authentication check: Session does not contain 'userId' attribute. Likely invalid session.");
                    return false;
                });
        logger.debug("Authentication check: User with ID {} is authenticated.", userId);
        return true; // Session exists and contains the userId attribute, user is authenticated
    }

    /**
     *
     * @param session
     * @return
     */
    public boolean isSessionExpired(HttpSession session) {
        Instant lastActivity = (Instant) session.getAttribute("lastActivity");
        if(lastActivity == null) {
                    logger.warn("Session has no last activity timestamp.");
                    return true; // consider session as expired, since no last activity found
        }
        int maxInactInterval = session.getMaxInactiveInterval();
        Instant expirationTime = lastActivity.plus(maxInactInterval, ChronoUnit.SECONDS);
        return Instant.now().isAfter(expirationTime);
    }
    public void authenticate(HttpServletRequest request) {
        logger.info("Authenticating user for request.");
        //
        if(!isAuthenticated(request)) {
            logger.warn("User is not authenticated, cannot perform actions");
            throw new IllegalStateException("User is not authenticated, cannot perform acions");
        }
        // sjekker om session finnes
        HttpSession session = request.getSession(false);
        if(session == null) {
            logger.error("No session found for user authentication.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"No active session found.");
        }
        // sjekker om user er lagret i session
        Object userObject = session.getAttribute("user");
        if(!(userObject instanceof User)) {
            logger.error("Session does not contain a valid user Object.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user session.");
        }
        // validerer bruker rolle
        User user = (User) userObject;
        if(!user.getRoles().contains(User.Role.ADMIN)) {
            logger.warn("User {} is not unathorized to perform this action.", user.getUsername());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not authorized to perform this action.");
        }
        logger.info("User {} is authorized to perform this action.", user.getUsername());
    }
}
