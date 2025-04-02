package com.example.versjon2.ExamUserTask.Service;

import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.Users;
import com.example.versjon2.ExamUserTask.Repository.UsersRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final Logger logger = LoggerFactory.getLogger(UsersService.class);

    /**
     * metode for å lagre en bruker
     * @param user
     * @return
     */
    @Transactional
    public Users saveUser(Users user) {
        String requestId = MDC.get("requestId");

        logger.info("Request ID: {} - Attempting saving user: {}", requestId, user);
        validateUserObject(user);
        ensureUserDoesnotExist(user);

        Users savedUser = usersRepository.save(user);
        logger.info("Request ID: {} - User with email {} successfully saved.", savedUser.getEmail());

        return savedUser;
    }

    private void validateUserObject(Users user) {
        String requestId = MDC.get("requestId");
        if(user == null) {
            logger.error("Request ID: {} - Attempted to save user with null user object.", requestId);
            throw new IllegalArgumentException("User Object cannot be null.");
        }
    }

    private void ensureUserDoesnotExist(Users user) {
        String requestId = MDC.get("requestId");
        usersRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            logger.error("Request ID: {} - Attempted to save a user that already exists in DB.", requestId);
            throw new IllegalArgumentException("User with email " + user.getEmail() +" already exists in DB.");
        });
    }
    @Transactional(readOnly = true)
    public List<Users> fetchAllUsers() {
        logger.info("Fetching all users from DB");
        List<Users> users = usersRepository.findAll();
        if(users.isEmpty()) {
            logger.info("No users found in the database");
            return Collections.emptyList();
        }
        logger.info("Recieved {} users.", users);
        return users;
    }

    @Transactional(readOnly = true)
    public List<Users> fetchAllUsersSortedByFirstNameAsc(boolean sortByFirstName) {
        logger.info("Fetching all users from DB, sorting by: {}", sortByFirstName);
        List<Users> users;
        if (sortByFirstName) {
            users = usersRepository.findAllByOrderByFirstNameAsc();
        } else {
            users = usersRepository.findAll();
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
    @Transactional(readOnly = true)
    public Page<UsersDTO> fetchAllUsersPaginated(Pageable pageable) {
        logger.info("Service Request: Fetching paginated users from DB - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Users> usersPage = usersRepository.findAll(pageable);
        logger.debug("Fetched {} Users entities from DB for page {} of {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages());

        return usersPage.map(UsersDTO::convertToDTO); // Convert each entity to DTO
    }

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


}

