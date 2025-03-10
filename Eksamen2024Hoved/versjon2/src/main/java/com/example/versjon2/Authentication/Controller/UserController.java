package com.example.versjon2.Authentication.Controller;

import com.example.versjon2.Authentication.Service.UserService;
import com.example.versjon2.Authentication.Repository.UserRepository;
import com.example.versjon2.Authentication.Loginrequest.LoginRequest;
import com.example.versjon2.Authentication.Entity.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            User user = userService.login(loginRequest.getUsername(), loginRequest.getPassword(), session);
            // Returnerer et JSON-objekt med informasjon om at innloggingen var vellykket
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User logged in successfully");
            response.put("username", user.getUsername());
            response.put("userId", user.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to login with username: {}", loginRequest.getUsername(), e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","invalid username or password"));
        }
    }


    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("message", "user logged out successfully");
            userService.logout(session);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error occured while logging out user with session ID {}: {}",session.getId(),e.getMessage(),e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message","Error occured while logging out user."));
        }
    }

    @GetMapping("/isLoggedIn/{userId}")
    public ResponseEntity<?> isLoggedInWithCookie(@PathVariable Long userId, HttpSession session) {
        try {
            boolean isLoggedIn = userService.isUserLoggedInWithCookies(session,userId);
            if(isLoggedIn) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "User is logged in."
                ));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "status", "error",
                        "message", "User is not logged in"
                ));
            }
        } catch (Exception e) {
            logger.error("Error occured while checikng login status.");
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "An error occured while checking login status: " + e.getMessage()));
        }
    }
    @GetMapping("/isLoggedIn")
    public ResponseEntity<?> isLoggedIn(HttpSession session) {
        try {
            boolean isLoggedIn = userService.isUserLoggedIn(session);
            if(isLoggedIn) {
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "User is logged in."
                ));
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "status", "error",
                        "message", "User is not logged in"
                ));
            }
        } catch (Exception e) {
            logger.error("Error occured while checikng login status.");
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "An error occured while checking login status: " + e.getMessage()));
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> register() {
        try {
            String rawPAssword = "passord123";
            String hashedPassword = passwordEncoder.encode(rawPAssword);
            User u = new User("test1", hashedPassword);
            logger.info("created user: " + u.getUsername() + " password: " + u.getPasswordHash());
            userRepository.save(u);
            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to register user.");
        }
    }

}


