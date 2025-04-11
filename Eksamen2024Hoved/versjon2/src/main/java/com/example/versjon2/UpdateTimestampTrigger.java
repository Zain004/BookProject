package com.example.versjon2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.h2.api.Trigger;

    public class UpdateTimestampTrigger implements Trigger {

        @Override
        public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before, int type) throws SQLException {
            // Her kan du initialisere triggeren (f.eks. sjekke om tabellen eksisterer)
        }

        @Override
        public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
            // Implementer logikken for å oppdatere updated_at her
            try {
                Timestamp currentTimestamp = Timestamp.from(LocalDateTime.now(ZoneOffset.UTC).toInstant(ZoneOffset.UTC));

                // Oppdater updated_at-kolonnen i newRow
                newRow[7] = currentTimestamp; // Forutsatt at updated_at er den 8. kolonnen (indeks 7)

                // Alternativt, oppdater direkte i databasen (kan være nødvendig i noen tilfeller)
                String sql = "UPDATE BOOKSQL SET updated_at = ? WHERE isbn_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setTimestamp(1, currentTimestamp);
                    pstmt.setLong(2, (Long) newRow[0]); // Forutsatt at isbn_id er den første kolonnen (indeks 0) og av typen Long
                    pstmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.err.println("Feil ved oppdatering av updated_at: " + e.getMessage());
                throw e; // Viktig å kaste SQLException videre, slik at transaksjonen rulles tilbake
            }
        }

}
