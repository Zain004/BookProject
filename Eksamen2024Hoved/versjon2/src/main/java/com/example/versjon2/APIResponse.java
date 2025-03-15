package com.example.versjon2;

import com.example.versjon2.ExamUserTask.DTO.UsersDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@ToString
@Value // genererer en toString og Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class APIResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final HttpStatus status;
    private final LocalDateTime timestamp;

    public static <T> ResponseEntity<APIResponse<T>> okResponse(T data, String message) {
        APIResponse<T> response = APIResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .status(HttpStatus.OK)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .headers(SecurityConfig.createSecurityHeaders())
                .body(response);
    }
    // funker kun med en tom liste, ikke fornuftig å bruke med paginering, da det kan skape forvirring
    public static <T> ResponseEntity<APIResponse<List<UsersDTO>>> noContentResponse(String message) {
        APIResponse<List<UsersDTO>> response = APIResponse.<List<UsersDTO>>builder()
                .success(true)
                .message(message)
                .status(HttpStatus.NO_CONTENT)
                .data(Collections.emptyList())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .headers(SecurityConfig.createSecurityHeaders())
                .body(response);
    }
}
