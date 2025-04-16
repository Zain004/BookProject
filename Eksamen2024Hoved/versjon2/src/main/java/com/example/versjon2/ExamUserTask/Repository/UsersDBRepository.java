package com.example.versjon2.ExamUserTask.Repository;

import com.example.versjon2.Book.Entity.BookSQL;
import com.example.versjon2.DatabaseException;
import com.example.versjon2.ExamUserTask.Entity.Users;
import com.example.versjon2.ExamUserTask.Entity.UsersDB;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@AllArgsConstructor
@Repository
public class UsersDBRepository {
    private static final Logger logger = LoggerFactory.getLogger(UsersDBRepository.class);
    private final JdbcTemplate jdbcTemplate;

    public int saveUser(UsersDB userdb) throws SQLException {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Attempting saving user: {}", requestId, userdb);
        //Users savedUser = usersRepository.save(userdb);
        String sql = "INSERT INTO USERSDB(first_name, last_name, dob, email, phone_number) VALUES (?, ?, ?, ?, ?)";

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
        userdb.setPhone(rs.getString("phone"));
        userdb.setEmail(rs.getString("email"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        userdb.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        userdb.setUpdatedAt(updatedAt != null ? updatedAt.toLocalDateTime() : null);
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
}
