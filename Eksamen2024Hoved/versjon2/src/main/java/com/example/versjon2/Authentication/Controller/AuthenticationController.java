package com.example.versjon2.Authentication.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.Authentication.LoginRequest;
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
    public ResponseEntity<APIResponse<Void>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        logger.info("Recieved login request for user: {}", loginRequest.getUsername());
        Optional<User> user = userService.login(loginRequest.getUsername(), loginRequest.getpassword(), request);
        logger.info("User {} successfully logged in.", loginRequest.getUsername());
        String message = "User " + loginRequest.getUsername() + " successfully logged in.";
        return APIResponse.okResponse(null, message);
    }

    /**
     * Endpoint for user logout
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public ResponseEntity<APIResponse<Void>> logout(HttpServletRequest request) {
        logger.info("Revieved logout request for session: {}", request.getSession());
        userService.logout(request);
        logger.info("Session {} successfully logged out.", request.getSession());
        String message = "User " + request.getSession().getId() + " successfully logged out.";
        return APIResponse.okResponse(null, message);
    }

    /**
     * Endpoint user is loggedin or not
     * @param request
     * @return
     */
    @GetMapping("/isLoggedIn")
    public ResponseEntity<APIResponse<Boolean>> isLoggedIn(HttpServletRequest request) {
        logger.info("Recieved isLoggedIn request for session: {}", request.getSession());
        boolean isLoggedIn = userService.isAuthenticated(request);
        logger.info("Login status checked for session: {}.", request.getSession());
        String message = "User " + request.getSession().getId() + " successfully logged in.";
        return APIResponse.okResponse(isLoggedIn, message);
    }
}
