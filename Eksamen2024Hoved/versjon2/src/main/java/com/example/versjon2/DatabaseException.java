package com.example.versjon2;

import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    /*
    Oppretter en database exception, fordi jeg ønsker rollback
    og videre kasting av APiResponse til klient.
     */
    private final ResponseEntity<APIResponse<ErrorInfo>> apiResponse;

    public DatabaseException(String message, SQLException cause, ResponseEntity<APIResponse<ErrorInfo>> apiResponse) {
        super(message, cause);
        this.apiResponse = apiResponse;
    }
    public ResponseEntity<APIResponse<ErrorInfo>> getApiResponse() {
        return apiResponse;
    }
}
