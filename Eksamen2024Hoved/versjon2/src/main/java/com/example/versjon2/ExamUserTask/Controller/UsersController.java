package com.example.versjon2.ExamUserTask.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.Users;
import com.example.versjon2.ExamUserTask.Service.UsersService;
import com.example.versjon2.PagedResponseDTO;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UsersController {

    private final UsersService usersService;

    private final Logger logger = LoggerFactory.getLogger(UsersController.class);

    /**
     *  lagrer en bruker
     * @param users
     * @return
     */
    @PostMapping
    public ResponseEntity<APIResponse<UsersDTO>> saveUser(@RequestBody @Valid Users users) {
        String requestId = UUID.randomUUID().toString(); // legger til en unik id for sporbarhet
        MDC.put("requestId", requestId);

        logger.info("Received request to save user: {}", users.getEmail()); // Logg kun relevant info
        Users savedUser = usersService.saveUser(users);

        UsersDTO usersDTO = UsersDTO.convertToDTO(savedUser);

        logger.info("User saved successfully with ID: {}", savedUser.getId());
        MDC.remove("requestId");

        return APIResponse.buildResponse(HttpStatus.CREATED, "User successfully created!", usersDTO);
    }

    /**
     *
     * @return list
     */

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

    /**
     * returnerer en liste som blir sortert med databasespørring i repository
     * @param sortByFirstName
     * @return
     */
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
     * denne er standard, returner en page med objekter
     * den kan også ta inn sort via javascript
     * @param pageable
     * @return
     */
    @GetMapping("/paged")
    public ResponseEntity<APIResponse<PagedResponseDTO<UsersDTO>>> getAllUsersPaginated(Pageable pageable) {
        logger.info("Recieved request to fetch all users with pageable: {}", pageable);
        Page<UsersDTO> usersPage = usersService.fetchAllUsersPaginated(pageable);

        PagedResponseDTO<UsersDTO> pagedResponseDTO;

        if (usersPage.isEmpty()) {
            logger.info("No users found for requsted page - Page: {}, Size: {}",
                    pageable.getPageNumber(), pageable.getPageSize());
            // tar inn page objekt og gjlr om til et PagedResponseDTO
            pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);
            return APIResponse.okResponse(pagedResponseDTO, "No users found. ");
        }

        logger.info("Successfully fetched {} users - Page {} of {}, Total Users: {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages(), usersPage.getTotalElements());
        pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);

        return APIResponse.okResponse(pagedResponseDTO, "Users successfully retrieved from DB.");
    }

    /** STANDARD PAGINERING MED FFORHÅNDSDEFINERT SORTERING
     * Denne metoden sorterer som standard på serversiden
     * du må opprette et Sort objekt og inkludere det i pageable uansett
     * om du skal bruke sorteringsretning som ASC eller DESC eller ikke
     * da må du velge en av disse som standard hvis du ikke bruker paramater,
     * men det beste er å bruke paramater
     * @param page
     * @param size
     * @param sortBy
     * @param sortDirection
     * @return
     */
    @GetMapping("/standardSort")
    public ResponseEntity<APIResponse<PagedResponseDTO<UsersDTO>>> getAllUsersPaginated(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "firstName") String sortBy, @RequestParam(defaultValue = "ASC") String sortDirection) {
        logger.info("Recieved request to fetch all users with page: {}, size: {}, sortBy: {}, sortDirection: {}", page, size, sortBy, sortDirection);
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UsersDTO> usersPage = usersService.fetchAllUsersPaginated(pageable);

        PagedResponseDTO<UsersDTO> pagedResponseDTO;

        if (usersPage.isEmpty()) {
            logger.info("No users found for requsted page - Page: {}, Size: {}",
                    pageable.getPageNumber(), pageable.getPageSize());
            // tar inn page objekt og gjlr om til et PagedResponseDTO
            pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);
            return APIResponse.okResponse(pagedResponseDTO, "No users found. ");
        }

        logger.info("Successfully fetched {} users - Page {} of {}, Total Users: {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages(), usersPage.getTotalElements());
        pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);

        return APIResponse.okResponse(pagedResponseDTO, "Users successfully retrieved from DB.");
    }

    /**
     * DENNE METODEN SORTERER OG FILTRERER VALGFRITT MED PARAMETRE
     * sorter etter fornavn, filter mellom 2 datoer
     * @param firstName
     * @param dobFrom
     * @param dobTo
     * @param pageable
     * @return
     */
    @GetMapping("/volunteer-opportunities/sort-and-filter")
    public ResponseEntity<APIResponse<PagedResponseDTO<UsersDTO>>> getAllUsersPaginated(
            @RequestParam(name="firstName", required = false) String firstName,
            @RequestParam(name="dobFrom", required = false) LocalDate dobFrom,
            @RequestParam(name="dobTo", required = false) LocalDate dobTo,
            Pageable pageable) {
        logger.info("Recieved request to fetch all users with filters - firstName: {}, dobFrom: {}, dobTo: {}, pageable: {}",
                firstName, dobFrom, dobTo, pageable);
        Page<UsersDTO> usersPage = usersService.fetchAllUsersFilteredAndSortedPaginated(firstName, dobFrom, dobTo, pageable);

        PagedResponseDTO<UsersDTO> pagedResponseDTO;

        if (usersPage.isEmpty()) {
            logger.info("No users found for requested criteria - Page: {}, Size: {}",
                    pageable.getPageNumber(), pageable.getPageSize());
            // tar inn page objekt og gjlr om til et PagedResponseDTO
            pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);
            return APIResponse.okResponse(pagedResponseDTO, "No users found. ");
        }

        logger.info("Successfully fetched {} users - Page {} of {}, Total Users: {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages(), usersPage.getTotalElements());
        pagedResponseDTO = PagedResponseDTO.fromPage(usersPage);

        return APIResponse.okResponse(pagedResponseDTO, "Users successfully retrieved from DB.");
    }

}


