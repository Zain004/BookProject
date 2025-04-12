package com.example.versjon2;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@AllArgsConstructor
@NoArgsConstructor(force = true)
public class UpdateTimestampTrigger {

    private static final Logger logger = LoggerFactory.getLogger(UpdateTimestampTrigger.class);

    private String tableName;

    private String idColumnName;

    private String updatedAtColoumnName;

    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        Timestamp currentTimestamp = Timestamp.from(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC));
        int idColumnIndex = findColoumnIndex(conn, tableName, idColumnName); // finn id kolonne
        int updatedAtColoumnIndex = findColoumnIndex(conn, tableName, updatedAtColoumnName); // finn updated_at kolonne
        //Oppdaterer 'newRow' (kan ikke garantere at dette synkroniseres automatisk med DB, men vi prøver)
        if (updatedAtColoumnIndex != -1) { // sjekker om updated_at finnes
            newRow[updatedAtColoumnIndex] = currentTimestamp;
        }
        String sql = "UPDATE " + tableName + " SET " + updatedAtColoumnName + " = ? WHERE " + idColumnName + " = ?";
        // bruker preparedStatement fordi vi forventet brukerinput, og vil beskyte mot sql injeciton
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, currentTimestamp);
            Object idValue = newRow[idColumnIndex]; // henter ID-verdien fra new row arrayet
            if (idValue instanceof Long) {
                pstmt.setLong(2, (Long) idValue);
            } else if (idValue instanceof Integer) {
                pstmt.setInt(2, (Integer) idValue);
            } else {
                throw new SQLException("Invalid id type: " + (idValue != null ? idValue.getClass().getName() : "null"));
            }
            pstmt.executeUpdate(); // brukes alltid for update, delete og insert
        } catch (SQLException e) {
            logger.error("Error updating timestamp in trigger", e);
            throw e; // **CRITICAL: Re-throw the SQLException**
        }
    }

    private int findColoumnIndex(Connection conn, String tableName, String columnName) throws SQLException {
        String sql = "SELECT * FROM " +  tableName + " WHERE 1=0"; // Trick: henter bare metadata, ingen faktiske rader
        // oppretter en try-with-resource blokk
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData(); // henter ut metadata

            int coloumnCount = metaData.getColumnCount(); // teller antall kolonner

            for (int i = 1; i <= coloumnCount; i++) {
                if (metaData.getColumnName(i).equals(columnName)) {
                    return i - 1; // ResultSet index er 1-basert, array index er 0 - basert
                }
            }
            logger.warn("Coloumn {} not found in table {}", columnName, tableName);
            return -1;
        }
    }

    /*
    SQL-spørringen "SELECT * FROM " + tableName + " WHERE 1=0" returnerer et tomt ResultSet.
    Selv om ResultSet er tomt (inneholder ingen rader), inneholder det metadata om tabellen,
    som kolonnenavn, datatyper og andre tabellattributter.  Denne teknikken brukes ofte til å
    effektivt hente tabellstrukturen uten å overføre faktiske data.  Eksemplet viser
    kolonneinformasjon fra en tabell 'MyTable':

    Metadata for table 'MyTable':
      Column 1: Name = ID, Type = INTEGER
      Column 2: Name = NAME, Type = VARCHAR
      Column 3: Name = AGE, Type = INTEGER
      Column 4: Name = CITY, Type = VARCHAR

    PreparedStatement: Bruk alltid PreparedStatement når du skal kjøre samme
    SQL-spørring flere ganger, uansett om parameterverdiene er faste eller variable.
    Og spesielt viktig er det å bruke PreparedStatement når
    parameterverdiene kommer fra brukerinput eller andre utrygge kilder. Dette er for å beskytte mot SQL-injeksjon.

    Statement: Bruk kun Statement for enkle, engangs SQL-spørringer der du er 100%
    sikker på at ingen del av spørringen kommer fra brukerinput eller andre
    utrygge kilder.
 */
}
