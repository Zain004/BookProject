package com.example.versjon2.ExamUserTask.Controller;

import com.example.versjon2.APIResponse;
import com.example.versjon2.ExamUserTask.DTO.UsersDBDTO;
import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.UsersDB;
import com.example.versjon2.ExamUserTask.Service.UsersDBService;
import com.example.versjon2.PagedResponseDTO;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/usersDB")
public class UsersDBController {

    private final UsersDBService usersService;

    private final Logger logger = LoggerFactory.getLogger(UsersDBController.class);

    /**
     *  lagrer en bruker
     * @param usersDB
     * @return
     */
    @PostMapping()
    public ResponseEntity<APIResponse<UsersDBDTO>> saveUser(@RequestBody @Valid UsersDB usersDB) throws SQLException {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Received request to save user: {}", requestId, usersDB); // Logg kun relevant info

        UsersDB savedUser = usersService.saveUser(usersDB);
        UsersDBDTO usersDTO = UsersDBDTO.convertToDTO(savedUser);

        logger.info("Request ID: {} - User saved successfully with ID: {}", requestId, savedUser.getId());
        return APIResponse.buildResponse(HttpStatus.CREATED, "User successfully created!", usersDTO);
    }

    /**
     *
     * @return list
     */

    @GetMapping("/list")
    public ResponseEntity<APIResponse<List<UsersDBDTO>>> getAllUsers() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Fetching all users from DB.", requestId);
        List<UsersDB> users = usersService.fetchAllUsersList(); // 1. Hent Users (ikke DTO)

        if(users.isEmpty()) {
            logger.info("Request ID: {} - No users found, returning 204 No Content", requestId);
            return ResponseEntity.noContent().build();
        }

        List<UsersDBDTO> usersDBDtos = UsersDBDTO.convertToDtoList(users);

        logger.info("Request ID: {} - Returning list of users to client.", requestId);
        return APIResponse.okResponse(usersDBDtos, "Users successfully retrieved from DB");
    }

    /**
     * returnerer en liste som blir sortert med databasespørring i repository
     * @param sortByFirstName
     * @return
     */

    @GetMapping("/list/sortedByFirstname")
    public ResponseEntity<APIResponse<List<UsersDBDTO>>> getAllUsersSortedByFirstName(
            @RequestParam(value = "sortByFirstname", required = false, defaultValue = "False") boolean sortByFirstName) {
            logger.info("Fetching all users from DB, sortByFirstname = {}", sortByFirstName);
            List<UsersDB> users = usersService.fetchAllUsersSortedByFirstNameAsc(sortByFirstName);
            List<UsersDBDTO> usersDTOs = List.of();

            if(users.isEmpty()) {
                logger.info("No users found, returning 204 No Content");
                return APIResponse.noContentResponse("No users found", usersDTOs);
            }
            usersDTOs = UsersDBDTO.convertToDtoList(users);

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
    public ResponseEntity<APIResponse<PagedResponseDTO<UsersDBDTO>>> getAllUsersPaginated(Pageable pageable) {
        logger.info("Recieved request to fetch all users with pageable: {}", pageable);
        Page<UsersDBDTO> usersPage = usersService.fetchAllUsersPaginated(pageable);

        PagedResponseDTO<UsersDBDTO> pagedResponseDTO;

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
    /*
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
    /*
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

    */
}


