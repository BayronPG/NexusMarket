package com.nexusmarket.infrastructure.web.dto;

import com.nexusmarket.application.query.PageResult;
import java.util.List;
import java.util.function.Function;

/**
 * Paged envelope of the REST responses.
 *
 * <p>Fase 2.9: the list endpoints return {@code content} plus the page metadata
 * documented in OpenAPI and exercised by the Postman collection.</p>
 *
 * @param content       elements of the page
 * @param page          zero based page number
 * @param size          page size
 * @param totalElements total elements matching the query
 * @param totalPages    total number of pages
 * @param <T>           response DTO type
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    /** Maps the application page into the web envelope. */
    public static <D, R> PageResponse<R> from(PageResult<D> result, Function<D, R> mapper) {
        return new PageResponse<>(result.content().stream().map(mapper).toList(),
                result.page(), result.size(), result.totalElements(), result.totalPages());
    }
}
