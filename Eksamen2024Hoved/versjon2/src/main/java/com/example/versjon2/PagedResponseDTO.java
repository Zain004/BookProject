package com.example.versjon2;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;
import java.util.*;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponseDTO<T> {
    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final Long totalElements;
    private final int totalPages;
    private final boolean isLast;
    private final boolean isFirst;
    // metoden tar inn et Objekt, en side med data
    public static <T> PagedResponseDTO<T> fromPage(Page<T> page) {
        return new PagedResponseDTO<>(
                page.getContent(), // <- Her henter vi innholdet!
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }
}
