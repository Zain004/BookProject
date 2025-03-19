package com.example.versjon2.ExamUserTask.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.Users;
import com.example.versjon2.ExamUserTask.Service.UsersService;
import com.example.versjon2.PagedResponseDTO;
import com.example.versjon2.SecurityConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @PostMapping("/saveUser")
    public ResponseEntity<?> saveUser(@RequestBody @Valid Users users) {
        usersService.saveUser(users);
        logger.info("Users saved successfully");
        return ResponseEntity.ok(Map.of(
                "success", "Users saved successfully"));

    }

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<UsersDTO>>> getAllUsers() {
        logger.info("Fetching all users from DB.");
        List<Users> users = usersService.fetchAllUsers(); // 1. Hent Users (ikke DTO)
        List<UsersDTO> userDTOs = UsersDTO.convertToDtoList(users);
        if(users.isEmpty()) {
            logger.info("No users found, returning 204 No Content");
            return APIResponse.noContentResponse("No users found");
        }
        logger.info("Returning list of users to client.");
        return APIResponse.okResponse(userDTOs, "Users successfully retrieved from DB");
    }

    // returnerer en liste som sorterer i databasen istedenfor på server
    @GetMapping("/list/sortedByFirstname")
    public ResponseEntity<APIResponse<List<UsersDTO>>> getAllUsersSortedByFirstName(
            @RequestParam(value = "sortByFirstname", required = false, defaultValue = "false") boolean sortByFirstName) {
            logger.info("Fetching all users from DB, sortByFirstname = {}", sortByFirstName);
            List<Users> users = usersService.fetchAllUsersSortedByFirstNameAsc(sortByFirstName);
            List<UsersDTO> usersDTOs = UsersDTO.convertToDtoList(users);

            if(users.isEmpty()) {
                logger.info("No users found, returning 204 No Content");
                return APIResponse.noContentResponse("No users found");
            }
            logger.info("Returning list of users to client.");
            return APIResponse.okResponse(usersDTOs, "Users successfully retrieved from DB");
    }

    /**
     * Bruk alltid queryparametre for paginaering
     * @param pageable
     * @return
     */
    @GetMapping("/users/paged")
    public ResponseEntity<APIResponse<PagedResponseDTO<UsersDTO>>> getAllUsersPaginated(Pageable pageable) {
        logger.info("Recieved request to fetch all users with pageable: {}", pageable);
        Page<UsersDTO> usersPage = usersService.fetchAllUsersPaginated(pageable);

        PagedResponseDTO<UsersDTO> pagedResponseDTO;

        if (usersPage.isEmpty()) {
            logger.info("No users found for requsted page - Page: {}, Size: {}",
                    pageable.getPageNumber(), pageable.getPageSize());
            pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);
            return APIResponse.okResponse(pagedResponseDTO, "No users found. ");
        }

        logger.info("Successfully fetched {} users - Page {} of {}, Total Users: {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages(), usersPage.getTotalElements());
        pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);

        return APIResponse.okResponse(pagedResponseDTO, "Users successfully retrieved from DB.");
    }

}


