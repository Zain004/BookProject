package com.example.versjon2.ExamUserTask.Service;

import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.UsersDB;
import com.example.versjon2.ExamUserTask.Repository.UsersDBRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UsersDBService {
    private final UsersDBRepository usersDbRepository;
    private final Logger logger = LoggerFactory.getLogger(UsersDBService.class);
    private JdbcTemplate jdbcTemplate;
    /**
     * metode for å lagre en bruker
     * @param
     * @return
     */
    @Transactional
    public UsersDB saveUser(UsersDB userdb) throws SQLException {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting saving user: {}", requestId, userdb);
        // validate that user is correct and does not exitst
        Assert.notNull(userdb, "Cannot insert null user");
        ensureUserDoesnotExist(userdb);

        int insertedRows = usersDbRepository.saveUser(userdb);
        logger.info("Request ID: {} - Successfully saved {} user to DB", requestId, insertedRows);

        UsersDB savedUser = userExistByEmail(userdb.getEmail());

        logger.info("Request ID: {} - User with email {} successfully saved.", userdb.getEmail());
        return savedUser;
    }

    public UsersDB userExistByEmail(String email) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Checking if user is saved successfully: {}", requestId, email);

        Optional<UsersDB> savedUser = usersDbRepository.getUserDB(email);

        if (savedUser.isEmpty()) {
            logger.info("Request ID: {} - Attempted to fetch user with email: {} after INSERT but no USER was found.", requestId, email);
            throw new EmptyResultDataAccessException("Book with id " + email + " not found.",1);
        }
        return savedUser.get();
    }

    private void ensureUserDoesnotExist(UsersDB usersDB) {
        String requestId = MDC.get("requestId");
        logger.info("Validating if user by email: {} exists in the database before insert.", requestId);

        String sql = "SELECT COUNT(*) FROM USERSDB WHERE email = ?";
        Integer getRows = jdbcTemplate.queryForObject(sql, Integer.class, usersDB.getEmail());

        if(getRows > 0) {
            logger.error("Request ID: {} - Attempted to insert user with duplicate email: {}", requestId, usersDB.getEmail());
            throw new IllegalArgumentException("User with email '" + usersDB.getEmail() + "' already exists in the database.");
        }
    }

    @Transactional(readOnly = true)
    public List<UsersDB> fetchAllUsersList() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Fetching all users from DB", requestId);

        List<UsersDB> users = usersDbRepository.getUserDBs();
        if(users.isEmpty()) {
            logger.info("Request ID: {} - No users found in the database", requestId);
            return Collections.emptyList();
        }

        logger.info("Request ID: {} - Recieved {} users.", requestId, users);
        return users;
    }

    @Transactional(readOnly = true)
    public List<UsersDB> fetchAllUsersSortedByFirstNameAsc(boolean sortByFirstName) {
        logger.info("Fetching all users from DB, sorting by: {}", sortByFirstName);
        List<UsersDB> users;
        if (sortByFirstName) {
            users = usersDbRepository.getUserDBsOrderByFirstNameAsc();
        } else {
            users = usersDbRepository.getUserDBs();
        }
         if(users.isEmpty()) {
             logger.info("No users found in the database");
             return Collections.emptyList();
         }
         logger.info("Recieved {} users.", users);
         return users;
    }

    /**
     * denne tar også inn sort
     * @param pageable
     * @return
     */
    /*
    @Transactional(readOnly = true)
    public Page<UsersDTO> fetchAllUsersPaginated(Pageable pageable) {
        logger.info("Service Request: Fetching paginated users from DB - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Users> usersPage = usersRepository.findAll(pageable);
        logger.debug("Fetched {} Users entities from DB for page {} of {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages());

        return usersPage.map(UsersDTO::convertToDTO); // Convert each entity to DTO
    }
/*
    @Transactional(readOnly = true)
    public Page<UsersDTO> fetchAllUsersFilteredAndSortedPaginated(String firstname, LocalDate dobFrom, LocalDate dobTo, Pageable pageable) {
        logger.info("Service Request: Fetching filtered and sorted paginated users from DB - firstname: {}," +
                "dobFrom: {}, dobTo: {}, Page: {}, Size: {}", firstname, dobFrom, dobTo, pageable.getPageNumber() + 1, pageable.getPageSize());
        Specification<Users> spec = createSpecification(firstname, dobFrom, dobTo);

        Page<Users> usersPage = usersRepository.findAll(spec, pageable);
        logger.debug("Fetched {} Users entities from DB for page {} of {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages());

        return usersPage.map(UsersDTO::convertToDTO);
    }

    private Specification<Users> createSpecification(String firstname, LocalDate dobFrom, LocalDate dobTo) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            // Null-sjekk og Tom streng-sjekk
            if(firstname != null && !firstname.isEmpty()) {
                // WHERE LOWER(firstname) LIKE "%john%"
                String searchTerm = "%" + firstname + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName"))
                        ,searchTerm)); // vil søke etter %firstName%
            }
            // AND
            if (dobTo != null && dobFrom != null) {
                if (dobFrom.isAfter(dobTo)) {
                 throw new IllegalArgumentException("dobFrom cannot be after dobTo"); // tidlig feilhåndteirng
                }
                predicates.add(criteriaBuilder.between(root.get("dob"), dobFrom, dobTo));
            } else if (dobFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dob"), dobFrom));
            } else if (dobTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dob"), dobTo));
            }
            // ORDER BY firstName ASC - Sorter basert på bruker input
            // kun sorter etter fornavn hvis den er fylt ut
            if (firstname != null && !firstname.isEmpty()) {
                query.orderBy(criteriaBuilder.asc(root.get("firstName")));
            }
// SELECT * FROM users WHERE LOWER(firstName) LIKE '%john%' AND dob = '1990-01-01' ORDER BY firstName ASC;
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
 */


}

