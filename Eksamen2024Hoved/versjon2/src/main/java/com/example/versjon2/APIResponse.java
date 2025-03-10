package com.example.versjon2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

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
}
