package com.example.versjon2.ExamUserTask.Service;

import com.example.versjon2.Authentication.UserEntity.User;
import com.example.versjon2.ExamUserTask.DTO.UsersDBDTO;
import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.example.versjon2.ExamUserTask.Entity.UsersDB;
import com.example.versjon2.ExamUserTask.Repository.UsersDBRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cglib.core.Local;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Transactional(readOnly = true)
    public Page<UsersDBDTO> fetchAllUsersPaginated(Pageable pageable) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Service Request: Fetching paginated users from DB - Page: {}, Size: {}",
                requestId, pageable.getPageNumber(), pageable.getPageSize());

        int pageNumber = pageable.getPageNumber(); // kun brukt for loggign
        int pageSize = pageable.getPageSize();
        long offSet = pageable.getOffset(); // de du vil hoppe over

        // 1. Hent totalt antall elementer (for Page-objektet)
        long totalElements = usersDbRepository.countElements();

        // 2. Hent data for gjeldende side
        List<UsersDB> usersDBS = usersDbRepository.getUersPage(pageSize, offSet);

        logger.debug("Fetched {} Users entities from DB for page {} of {}",
                usersDBS.size(), pageNumber + 1, (totalElements + pageSize - 1) / pageSize); // Calculate total pages

        Page<UsersDB> page = new PageImpl<>(usersDBS, pageable, totalElements);
        return page.map(UsersDBDTO::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<UsersDBDTO> fetchAllUsersPaginatedWithSortAndDirection(Pageable pageable) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Service Request: Fetching paginated users from DB - Page: {}, Size: {}",
                requestId, pageable.getPageNumber(), pageable.getPageSize());

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        long offSet = pageable.getOffset();
        // Hent listen over sorteringskriterier
        List<String> sortFields = new ArrayList<>();
        Optional<String> sortDirection = Optional.empty();

        if (pageable.getSort().isSorted()) {
            List<Sort.Order> sortOrders = pageable.getSort().toList();
            for (Sort.Order order : sortOrders) {
                logger.info("Request ID: {} - Sort field: {}, Sort direction: {}", requestId, order.getProperty(), order.getDirection());
                sortFields.add(order.getProperty());
                sortDirection = Optional.of(order.getDirection().name());
            }
        }
        logger.info("Request ID: {} - Sort fields: {}, Sort direction: {}", requestId, sortFields, sortDirection);

        // 1. Hent totalt antall elementer (for Page-objektet)
        long totalElements = usersDbRepository.countElements();

        // 2. Hent data for gjeldende side
        List<UsersDB> usersDBS = usersDbRepository.getUersPageOrderByAndDirection(pageSize,offSet, sortFields, sortDirection);


        logger.debug("Request ID: {} - Fetched {} Users entities from DB for page {} of {}",
                usersDBS.size(), pageNumber + 1, (totalElements + pageSize - 1) / pageSize); // Calculate total pages

        Page<UsersDB> page = new PageImpl<>(usersDBS, pageable, totalElements);
        return page.map(UsersDBDTO::convertToDTO);
    }

    /**
     * Denne metoden filtrerer etter fornavn og henter navn som er
     * mellom disse 2 valgte datoene og sorterer manuels basert på fornavn.
     * Den bruker en egen klasse for å lage spørringen fordi spørringen blir kompleks.
     * I tillegg har den en egen private metode som har isntans til denne klassen
     * og genererer spørringen og setter inn i klassens objekt og gir objektet.
     * Så bruker vi dette objektet videre i repository metodene
     * @param firstname
     * @param dobFrom
     * @param dobTo
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<UsersDBDTO> fetchAllUsersFilteredAndSortedPaginated(String firstname, LocalDate dobFrom, LocalDate dobTo, Pageable pageable) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Service Request: Fetching filtered and sorted paginated users from DB - firstname: {}," +
                "dobFrom: {}, dobTo: {}, Page: {}, Size: {}", requestId, firstname, dobFrom, dobTo, pageable.getPageNumber() + 1, pageable.getPageSize());

        // Steg 1: oppretter spørrringen med en egen metode
        SqlQueryWithParams sqlQueryWithParams = createSqlQueryWithParams(firstname, dobFrom, dobTo, pageable);

        // Steg 2: henter data fra DB med via repository-metoden getUsersFilteredAndSortedPAginated
        List<UsersDB> users = usersDbRepository.getUsersFilteredAndSortedPAginated(sqlQueryWithParams);

        // 1. Hent totalt antall elementer (for Page-objektet)
        long total = usersDbRepository.getCountForFilteredElements(sqlQueryWithParams);
        logger.debug("Fetched {} Users entities from DB for page {} of {}",
                users.size(), pageable.getPageNumber() + 1, (total + pageable.getPageSize() - 1) / pageable.getPageSize());

        Page<UsersDB> page = new PageImpl<>(users, pageable, total);
        return page.map(UsersDBDTO::convertToDTO);
    }

    @AllArgsConstructor
    @ToString
    @Getter
    @Setter
    public static class SqlQueryWithParams {
        String sql;
        List<Object> params;
        String countWhereClause;
        List<Object> countParams;
    }

    /**
     *
     * @param firstName
     * @param dobFrom
     * @param dobTo
     * @param pageable, tar inn dette fordi må bruke LIMIT ? OFFSET ?
     * @return
     */
    private SqlQueryWithParams createSqlQueryWithParams(String firstName, LocalDate dobFrom, LocalDate dobTo, Pageable pageable) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Service Request: Creating SQL query with params Firstname: {}, dobFrom: {}, dobTo: {}.", requestId, firstName, dobFrom, dobTo);
        StringBuilder sql = new StringBuilder("SELECT * FROM USERSDB");
        StringBuilder whereClause = new StringBuilder(); // en WHERE clausul brukes for å filtrere rader

        List<Object> params = new ArrayList<>();
        List<Object> countParams = new ArrayList<>();

        // WHERE klauselen
        if (firstName != null && !firstName.isEmpty()) {
            whereClause.append(" WHERE first_name ILIKE ? ");
            params.add("%" + firstName + "%");
            countParams.add("%" + firstName + "%");
        }
     /* Eksempel:
        Hvis firstName = "ann", så blir SQL-spørringen noe sånt som:
        SELECT * FROM person WHERE first_name ILIKE '%ann%'
        Denne spørringen vil matche navn som: "Ann", "Joanna", "Brianna"
         */
        if (dobFrom != null && dobTo != null) {
            if (dobFrom.isAfter(dobTo)) {
                logger.warn("Request ID: {} - dobFrom cannot be after dobTo.");
                throw new IllegalArgumentException("dobFrom cannot be after dobTo");
            }
            whereClause.append(whereClause.length() > 0 ? " AND " : " WHERE ");
            whereClause.append(" dob BETWEEN ? AND ? ");
            params.add(dobFrom);
            params.add(dobTo);
            countParams.add(dobFrom);
            countParams.add(dobTo);
        } else if (dobFrom != null) {
            whereClause.append(whereClause.length() > 0 ? " AND " : " WHERE ");
            whereClause.append(" dob >= ? ");
            params.add(dobFrom);
        } else if (dobTo != null) {
            whereClause.append(whereClause.length() > 0 ? " AND " : " WHERE ");
            whereClause.append(" dob <= ? ");
            params.add(dobTo);
        }
        sql.append(whereClause);
        // ORDER BY CLAUSE
        if (firstName != null && !firstName.isEmpty()) {
            sql.append(" ORDER BY first_name ASC ");
        }
        sql.append(" LIMIT ? OFFSET ?");
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());
        return new SqlQueryWithParams(sql.toString(), params, whereClause.toString(), countParams);
    }





}

