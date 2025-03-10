package com.example.versjon2.Authentication.Service;

import com.example.versjon2.Authentication.Repository.UserRepository;
import com.example.versjon2.Authentication.Entity.User;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@AllArgsConstructor
@Service
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private Logger logger = LoggerFactory.getLogger(UserService.class);
    // login bruker
    @Autowired // Injiser PasswordEncoder
    private PasswordEncoder passwordEncoder;
    @Transactional
    public User login(String username, String password, HttpSession session) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        // sjekk om brukeren finnes og om passordet matcher
        return optionalUser.filter(user -> {
            if(!passwordEncoder.matches(password, user.getPasswordHash())) {
                logger.warn("invalid password for username '{}'", user.getUsername());
                return false;
            }
            return true;
            }).map(user -> { // sjekk om brukeren allerede er innlogget i databasen
                    if(user.isLoggedIn()) {
                        logger.warn("User '{}' is already logged in the database.",user.getUserId());
                        user.setLoggedIn(false);
                        userRepository.save(user);
                    }// Sjekk om brukeren allerede er innlogget i sesjonen
                    Boolean sessionLoggedIn = (Boolean) session.getAttribute("loggedIn") ;
                    if(Boolean.TRUE.equals(sessionLoggedIn)) {
                        logger.warn("User '{}' is already logged in this session.", user.getUserId());
                    }
                    // hvis optional user eksisterer og passord er riktig
                    // lagre innloggingsstatus i database
                    user.setLoggedIn(true); // Logg inn og oppdater statusen til "loggedIn
                    userRepository.save(user); // Lagre endirnger i databasen
                    // lagre innloggingsstatus i browser
                    session.setAttribute("userId", user.getUserId()); // lagrer brukerens unike Id i HTTP sesjonen, nyttig når du trenger å vite hvilken bruker som utfører operasjonen
                    session.setAttribute("loggedIn", true); // lagrer en loggedIn status boolsk verdi i HTTP-sesjonen, som indikerer at brukeren er logget inn elelr ikke i applikasjonen
                    // logg inn suksessfullt
                    logger.info("User {} logged in successfully with username '{}'", user.getUserId(), username);
                    return user;
                })
                .orElseThrow(() -> {
                    logger.warn("Failed login attempt for username: {" + username +"}");
                    return new RuntimeException("Invalid username or password."); // kast er spesifikt unntak
                });
    }

    // Utlogging av bruker
    @Transactional
    public void logout(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            // Bruk den innebygde findById-metoden fra JpaRepository
            userRepository.findById(userId).ifPresent(user -> {
                if(user.isLoggedIn()) {
                    user.setLoggedIn(false); // Oppdaterer brukerens "loggedIn"-status i databasen
                    userRepository.save(user); // Lagre endringene i databasen
                    logger.info("User with ID {" + userId + "} has logged out.");
                }
            });
        } else {
            // hvis userId ikke finnes i sesion, logg en advarsel
            logger.warn("User attempted to logout without a valid userId in session");
        }
        session.invalidate(); // Fjern all session-informasjon
        logger.info("Session invalidated for user with ID {" + userId + "}");
    }

    public boolean isUserLoggedInWithCookies(HttpSession session, Long userId) {
        if(session == null) {
            logger.warn("Session is null for user with ID '{}'", userId);
            return false; // Returner false hvis session er null
        }
        // Sjekk om sesjonen er gyldig
        Boolean isSessionValid = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
        logger.debug("Session validity for user ID '{}' is '{}'", userId, isSessionValid);

        // Dobeltsjekk status i databasen
        boolean isDatabaseValid = userRepository.findById(userId)
                .map(User::isLoggedIn)
                .orElse(false);
        logger.debug("Database validity for user ID '{}' is '{}'", userId, isDatabaseValid);

        boolean result = isSessionValid && isDatabaseValid;
        if(!result) {
            logger.warn("User Id '{}' is not logged in. Session valid: '{}', Database valid: '{}'", userId, isSessionValid, isDatabaseValid);
        }
        return result;
    }
    public boolean isUserLoggedIn(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if(userId == null) {
            logger.warn("No userId found in session");
            return false;
        }
        if(session == null) {
            logger.warn("Session is null for user with ID '{}'", userId);
            return false; // Returner false hvis session er null
        }
        // Sjekk om sesjonen er gyldig
        Boolean isSessionValid = Boolean.TRUE.equals(session.getAttribute("loggedIn"));
        logger.debug("Session validity for user ID '{}' is '{}'", userId, isSessionValid);

        // Dobeltsjekk status i databasen
        boolean isDatabaseValid = userRepository.findById(userId)
                .map(User::isLoggedIn)
                .orElse(false);
        logger.debug("Database validity for user ID '{}' is '{}'", userId, isDatabaseValid);

        boolean result = isSessionValid && isDatabaseValid;
        if(!result) {
            logger.warn("User Id '{}' is not logged in. Session valid: '{}', Database valid: '{}'", userId, isSessionValid, isDatabaseValid);
        }
        return result;
    }
}
