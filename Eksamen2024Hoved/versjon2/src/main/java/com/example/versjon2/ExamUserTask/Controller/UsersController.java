package com.example.versjon2.ExamUserTask.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.Users;
import com.example.versjon2.ExamUserTask.Service.UsersService;
import com.example.versjon2.SecurityConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UsersController {

    private final UsersService usersService;

    private final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<UsersDTO>>> getAllUsers() {
        logger.info("Fetching all users from DB.");
        List<Users> users = usersService.fetchAllUsers(); // 1. Hent Users (ikke DTO)
        List<UsersDTO> userDTOs = UsersDTO.convertToDtoList(users);
        if(users.isEmpty()) {
            logger.info("No users found, returning 204 No Content");
            return noContentResponse("No users found");
        }
        logger.info("Returning list of users to client.");
        return okResponse(userDTOs, "Users successfully retrieved from DB");
    }
    private <T> ResponseEntity<APIResponse<T>> okResponse(T data, String message) {
        APIResponse<T> response = APIResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .headers(SecurityConfig.createSecurityHeaders())
                .body(response);
    }
    private <T> ResponseEntity<APIResponse<List<UsersDTO>>> noContentResponse(String message) {
        APIResponse<List<UsersDTO>> response = APIResponse.<List<UsersDTO>>builder()
                .success(true)
                .message(message)
                .status(HttpStatus.NO_CONTENT)
                .data(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .headers(SecurityConfig.createSecurityHeaders())
                .body(response);
    }


    @PostMapping("/saveUser")
    public ResponseEntity<?> saveUser(@RequestBody @Valid Users users) {
        usersService.saveUser(users);
        logger.info("Users saved successfully");
        return ResponseEntity.ok(Map.of(
                "success", "Users saved successfully"));

    }
}
