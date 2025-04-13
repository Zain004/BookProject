package com.example.versjon2.Book.Repository;


import com.example.versjon2.Book.Entity.BookSQL;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class BookSQLRepository {
    private final Logger logger = LoggerFactory.getLogger(BookSQLRepository.class);
    private JdbcTemplate jdbcTemplate;

    /**
     * Denne metoden oppdaterer hvert enkelt element separat, fordi jeg ønsker
     * å hente ut id'ene til hvert element pg returnere en DTO til klient.
     * hvis jeg ikke ville returnert en helt vanlig dto uten id kunne jeg brukt
     * batch, men ehr ønsker jeg å også returnere id så derfor henter jeg den
     * manuelt ut med en keyholder
     * @param books
     * @return
     */
    public List<BookSQL> saveAll(List<BookSQL> books) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Saving books through book repository: {}", requestId, books);

        String sql = "INSERT INTO BOOKSQL (title, author, publishing_year, rating, category) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyholder = new GeneratedKeyHolder();
        int rowsInserted = 0;
        // implementerer BatchPreparedStatements interface
        for (BookSQL book : books) {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            // bruker en preparedstatement for åm unngå sql injection
            PreparedStatementCreator psmt = new PreparedStatementCreator() {
                // utfører selve innsettingen
                @Override
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                    ps.setString(1, book.getTitle());
                    ps.setString(2, book.getAuthor());
                    ps.setInt(3, book.getPublishingYear());
                    ps.setDouble(4, book.getRating());
                    ps.setString(5, book.getCategory());
                    return ps;
                }
            };
            // bruker kun keyholder fordi jeg trenger den genererte id'en
            jdbcTemplate.update(psmt, keyHolder); // oppdaterer databasen
            // **EKSTRA LOGGING (FØR HENTING):** Logg KeyHolder-informasjon
            logger.info("Request ID: {} - KeyHolder keys: {}", requestId, keyHolder.getKeys()); // Eller keyHolder.getKeyList()
            Long isbnId = extractIsbnId(keyHolder, book);

            if (isbnId != null) {
                book.setIsbnId(isbnId);
                rowsInserted++;
                logger.info("Request ID: {} - Successfully saved book with ID: {}", requestId, book.getIsbnId());
            } else {
                logger.warn("Request ID: {} - Could not get generated ID for Book but successfully inserted {}.", requestId, book);
            }
        }
        logger.info("Request ID: {} - Successfully saved {} books.", requestId, (Integer) rowsInserted);
        return books;
    }

    private Long extractIsbnId(KeyHolder keyHolder, BookSQL book) {
        String requestId = MDC.get("requestId");
        logger.info("Request ID: {} - Extracting isbn ID from book: {}", requestId, book);
        List<Map<String, Object>> keyList = keyHolder.getKeyList();
        if (keyList == null || keyList.isEmpty()) {
            logger.warn("Request ID: {} - Could not get generated ID for Book {}. KeyList is null or empty.", requestId, book);
            return null; // Eller kast en exception hvis det er mer passende
        }
        // Hent første Map fra listen
        Map<String, Object> keys = keyList.get(0);

        Object isbnIdObject = keys.get("isbn_id");

        // sjekk at isnnIdObject er en Number
        if (!(isbnIdObject instanceof Number)) {
            logger.error("Request ID: {} - Unexpected type for isbn_id: {}. Expected Number, got {}.", requestId, isbnIdObject, isbnIdObject.getClass().getName());
            return null; // eller kast exceptio
        }
        return ((Number) isbnIdObject).longValue();
    }

    /**
     * Denne metoden er kjappere og oppdaterer i batcher og ikke hver for seg,
     * den er bra hvis du ikke trenger å returnere id'en via dto til klient.
     * I tillegg returnerer den spesifikt hvor mange rader som er blitt oppdatert.
     * @param books
     * @return
     */
    public int saveBatch(List<BookSQL> books) {
        String requestId = MDC.get("requestId"); // Hent requestId fra MDC
        logger.info("Request ID: {} - Saving batch through book repository: {}", requestId, books);

        String sql = "INSERT INTO BOOKSQL (title, author, publishing_year, rating, category) VALUES (?, ?, ?, ?, ?)";

        int[] updatedColumns = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BookSQL book = books.get(i);
                ps.setString(1, book.getTitle());
                ps.setString(2, book.getAuthor());
                ps.setInt(3, book.getPublishingYear());
                ps.setDouble(4, book.getRating());
                ps.setString(5, book.getCategory());
            }
            @Override
            public int getBatchSize() {
                return books.size();
            };
        });

        int rowsInserted = updatedColumns.length;
        logger.info("Request ID: {} - Successfully saved {} books.", rowsInserted);

        return rowsInserted;
    }
}
