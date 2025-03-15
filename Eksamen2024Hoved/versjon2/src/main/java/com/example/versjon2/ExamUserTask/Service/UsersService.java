package com.example.versjon2.ExamUserTask.Service;

import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.Users;
import com.example.versjon2.ExamUserTask.Repository.UsersRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final Logger logger = LoggerFactory.getLogger(UsersService.class);

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
     *
     * @param pageable
     * @return
     */
    public Page<UsersDTO> fetchAllUsersPaginated(Pageable pageable) {
        logger.info("Service Request: Fetching paginated users from DB - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<Users> usersPage = usersRepository.findAll(pageable);
        logger.debug("Fetched {} Users entities from DB for page {} of {}",
                usersPage.getContent().size(), usersPage.getNumber() + 1, usersPage.getTotalPages());

        return usersPage.map(UsersDTO::convertToDTO); // Convert each entity to DTO
    }



    // Metode for å lagre en bruker

    public Users saveUser(Users user) {
        if(user == null) {
            logger.error("Attempted to save user with null user object.");
            throw new IllegalArgumentException("User Object cannot be null.");
        }
        // sjekker om brukeren allerede finnes (GLobalExceptionHandler håndterer unntaket)
        usersRepository.findByEmail(user.getEmail()).ifPresent(existingUser -> {
            logger.error("Attempted to a user that already exists in DB.");
            throw new IllegalArgumentException("User with email " + user.getEmail() +" already exists in DB.");
        });

        Users savedUser = usersRepository.save(user);
        logger.info("User with email {} successfully saved.", savedUser.getEmail());
        return savedUser;
    }
}

