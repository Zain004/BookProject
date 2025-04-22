package com.example.versjon2.CommonItems.Exceptions;

import com.example.versjon2.CommonItems.Response.APIResponse;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

public class DatabaseException extends RuntimeException {
    /*
    Oppretter en database exception, fordi jeg ønsker at SQL EXception
    skal bli automatisk videre kastet av global handler til en database exception.
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
