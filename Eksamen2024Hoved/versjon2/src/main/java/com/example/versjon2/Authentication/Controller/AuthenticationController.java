package com.example.versjon2.Authentication.Controller;

import com.example.versjon2.Authentication.Service.UserService;
import com.example.versjon2.Authentication.UserEntity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    /**
     * Endpoint for user login
     * @param loginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<APiResponse<Object>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        logger.info("Recieved login request for user: {}", loginRequest.getUsername());
        Optional<User> user = userService.login(loginRequest.getUsername(), loginRequest.getpassword(), request);
        logger.info("User {} successfully logged in.", loginRequest.getUsername());
        return ResponseEntity.ok(new APiResponse<>("Successfully logged in with username: " + loginRequest.getUsername(), null, user));
    }

    /**
     * Endpoint for user logout
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<APiResponse<Object>> logout(HttpServletRequest request) {
        logger.info("Revieved logout request for session: {}", request.getSession());
        userService.logout(request);
        logger.info("Session {} successfully logged out.", request.getSession());
        return ResponseEntity.ok(new APiResponse<>("Session " + request.getSession() + " is successfully logged out.", null, null));
    }

    /**
     * Endpoint user is loggedin or not
     * @param request
     * @return
     */
    @GetMapping("/isLoggedIn")
    public ResponseEntity<APiResponse<Boolean>> isLoggedIn(HttpServletRequest request) {
        logger.info("Recieved isLoggedIn request for session: {}", request.getSession());
        boolean isLoggedIn = userService.isAuthenticated(request);
        logger.info("Login status checked for session: {}.", request.getSession());
        return ResponseEntity.ok(new APiResponse<>("User status logged in: " + isLoggedIn, null, isLoggedIn));
    }
}
