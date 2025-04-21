package com.example.versjon2.ExamUserTask.Repository;

import com.example.versjon2.ExamUserTask.Entity.UsersDB;
import com.example.versjon2.ExamUserTask.Service.UsersDBService;
import lombok.AllArgsConstructor;
import org.apache.coyote.http11.upgrade.UpgradeProcessorInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.relational.core.sql.SQL;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Repository
public class UsersDBRepository {
    private static final Logger logger = LoggerFactory.getLogger(UsersDBRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public int saveUser(UsersDB userdb) throws SQLException {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting saving user: {}", requestId, userdb);
        //Users savedUser = usersRepository.save(userdb);
        String sql = "INSERT INTO USERSDB(first_name, last_name, dob, email, phone) VALUES (?, ?, ?, ?, ?)";

        int insertedRows = jdbcTemplate.update(sql, ps -> {
            ps.setString(1, userdb.getFirstName());
            ps.setString(2, userdb.getLastName());
            ps.setDate(3, Date.valueOf(userdb.getDob()));
            ps.setString(4, userdb.getEmail());
            ps.setString(5, userdb.getPhone());
        });

        if (insertedRows != 1) {
            logger.error("Request ID: {} - Failed to insert user with email: {} into database, Already exists or invalid argument.", requestId, userdb.getEmail());
            throw new SQLException("Insertion of user affected " + insertedRows + " rows, expected 1 for email: " + userdb.getEmail());
        }

        logger.info("Request ID: {} - User with email {} successfully saved USer to DB.", requestId, userdb.getEmail());
        return insertedRows;
    }

    public static final RowMapper<UsersDB> usersDBRowMapper = (ResultSet rs, int rowNum) -> {
        UsersDB userdb = new UsersDB();
        userdb.setId(rs.getLong("id"));

        userdb.setFirstName(rs.getString("first_name"));
        userdb.setLastName(rs.getString("last_name"));

        java.sql.Date dob = rs.getDate("dob");
        userdb.setDob(dob.toLocalDate());

        userdb.setPhone(rs.getString("phone"));
        userdb.setEmail(rs.getString("email"));

        OffsetDateTime createdAtOffset = rs.getObject("created_at", OffsetDateTime.class);
        if (createdAtOffset != null) {
            userdb.setCreatedAt(createdAtOffset.toLocalDateTime());
        }

        OffsetDateTime updatedAtOffset = rs.getObject("updated_at", OffsetDateTime.class);
        if (updatedAtOffset != null) {
            userdb.setUpdatedAt(updatedAtOffset.toLocalDateTime());
        }

        return userdb;
    };

    public Optional<UsersDB> getUserDB(String email) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting fetching user with email {} from DB", requestId, email);

        String sql = "SELECT * FROM USERSDB WHERE email = ?";
        UsersDB usersDB = jdbcTemplate.queryForObject(sql, new Object[]{email}, usersDBRowMapper);

        logger.debug("Request ID: {} - Successfully fetched USer with email: {} from database. User details: {}", requestId, usersDB.getEmail(), usersDB);
        return Optional.of(usersDB);
    }

    public List<UsersDB> getUserDBs() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting fetching all users from DB", requestId);

        String sql = "SELECT * FROM USERSDB";
        List<UsersDB> usersDBs = jdbcTemplate.query(sql, usersDBRowMapper);

        logger.debug("Request ID: {} - Successfully fetched all Users: {} from DB", requestId, usersDBs);
        return usersDBs;
    }

    public List<UsersDB> getUserDBsOrderByFirstNameAsc() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting fetching all users from DB Sorting by firstname.", requestId);

        String sql = "SELECT * FROM USERSDB ORder BY first_name ASC";
        List<UsersDB> usersDBs = jdbcTemplate.query(sql, usersDBRowMapper);

        logger.debug("Request ID: {} - Successfully fetched all Users: {} from DB sorted by firstname", requestId, usersDBs);
        return usersDBs;
    }

    public long countElements() {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting counting all users from DB", requestId);
        String sql = "SELECT COUNT(*) FROM USERSDB";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        logger.info("Request ID: {} - Successfully fetched all Users: {} from DB", requestId, count);
        return count;
    }

    /**
     * Denne metoden er standard paginering, Ganske simpel.
     * Den paginerer korrekt men sorterer ikke eller filtrerer ikke
     * @param pageSize
     * @param offset
     * @return
     */
    public List<UsersDB> getUersPage(int pageSize, long offset) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting fetching page {} of {} users from DB", requestId, pageSize, offset);
        String sql = "SELECT * FROM USERSDB LIMIT ? OFFSET ?";
        List<UsersDB> usersDBs = jdbcTemplate.query(sql, new Object[]{pageSize, offset}, usersDBRowMapper);
        logger.info("Request ID: {} - Successfully fetched page {} of {} Users: {} from DB", requestId, pageSize, offset, usersDBs);
        return usersDBs;
    }

    /**
     * Denne metoden er bedre, den sorterer med flere parametre og
     * har en private metode som den sjekker opp mot om sorteringskolonnen er gyldig.
     * I tillegg hvis du har behov for sortering bruk denne hvis du ikke har behov
     * bruk metoden over, fordi den er ganske rett ffrem uten sortering
     * @param pageSize
     * @param offset
     * @param sortBy
     * @param sortDirection
     * @return
     */
    public List<UsersDB> getUersPageOrderByAndDirection(int pageSize, long offset, List<String> sortBy, Optional<String> sortDirection) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting fetching page {} of {} users from DB.", requestId, pageSize, offset);
        logger.info("Request ID: {} - Sorting by: {}. Sorting direction: {}", requestId, sortBy, sortDirection);
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM USERSDB");

        if (!sortBy.isEmpty()) {
            // White-list tillatte kolonner for å forhindre SQL-injeksjon
            List<String> validSortColumsns = sortBy.stream()
                    .map(String::trim)
                    .filter(sortColumn -> !sortColumn.isEmpty() && isValidSortColumn(sortColumn))
                    .collect(Collectors.toList());

            if (validSortColumsns.isEmpty()) {
                logger.warn("Request ID: {} - Invalid sort column, applying ordering by id: .  Ignoring sort order.", requestId);
                sqlBuilder.append(" ORDER BY ").append(" id");
            } else {
                for (int i = 0; i < validSortColumsns.size(); i++) {
                    String sortColumn = validSortColumsns.get(i);
                    if (i == 0) {
                        sqlBuilder.append(" ORDER BY ");
                    } else {
                        sqlBuilder.append(", ");
                    }
                    sqlBuilder.append(sortColumn).append(" ");
                }

            }
            // Velger sorteringsretning
            if (sortDirection.isPresent()) {
                String direction = sortDirection.get().trim().toUpperCase();
                if (direction.equals("ASC") || direction.equals("DESC")) {
                    sqlBuilder.append(" ").append(direction);
                } else {
                    logger.warn("Request ID: {} - Invalid sort direction: {}. Using default ASC.", requestId, direction);
                    sqlBuilder.append(" ASC");
                }
            }
        }
        logger.info("Request ID: {} - Final SQL: {}", requestId, sqlBuilder.toString());
        sqlBuilder.append(" LIMIT ? OFFSET ?");
        String sql = sqlBuilder.toString();
        List<UsersDB> usersDBs = jdbcTemplate.query(sql, new Object[]{pageSize, offset}, usersDBRowMapper);
        logger.info("Request ID: {} - Successfully fetched page {} of {} Users: {} from DB", requestId, pageSize, offset, usersDBs);
        return usersDBs;
    }

    private static final Set<String> VALID_SORT_COLUMNS =
            Set.of("id", "first_name", "last_name", "dob", "phone", "email", "created_at", "updated_at");

    private boolean isValidSortColumn(String sortColumn) {
        return VALID_SORT_COLUMNS.contains(sortColumn.trim().toLowerCase());
    }

    public List<UsersDB> getUsersFilteredAndSortedPAginated(UsersDBService.SqlQueryWithParams sqlQueryWithParams) {
    String requestId = MDC.get("requestId");
    logger.info("Request ID: {} - Attempting Fetching users Filtered by params: ", requestId, sqlQueryWithParams.toString());

    List<UsersDB> usersDBS = jdbcTemplate.query(sqlQueryWithParams.getSql(), sqlQueryWithParams.getCountParams().toArray(new Object[0]), usersDBRowMapper);
    logger.info("Request ID: {} - Successfully fetched {} Users: {} from DB", requestId, usersDBS.size(), usersDBS);

    return usersDBS;
    }

    public long getCountForFilteredElements(UsersDBService.SqlQueryWithParams sqlQueryWithParams) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting getting totalElements for query: ", requestId, sqlQueryWithParams.toString());

        String countQuery = "SELECT COUNT(*) FROM USERSDB " + sqlQueryWithParams.getCountWhereClause();
        long total = jdbcTemplate.queryForObject(countQuery, sqlQueryWithParams.getCountParams().toArray(new Object[0]), long.class);

        logger.info("Request ID: {} - Successfully fetched totalElements: {} from DB", requestId, total);
        return total;
    }
}
