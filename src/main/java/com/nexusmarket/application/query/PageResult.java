package com.nexusmarket.application.query;

import java.util.List;

/**
 * Framework independent page of results.
 *
 * <p>INTERPRETATION: the application layer exposes the page metadata the REST
 * contract needs (page, size, total elements, total pages) without leaking
 * Spring Data types.</p>
 *
 * @param content       elements of the requested page
 * @param page          zero based page number
 * @param size          requested page size
 * @param totalElements total elements matching the query
 * @param totalPages    total number of pages
 * @param <T>           element type
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public PageResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    /** Builds a page and derives {@code totalPages} from the total. */
    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, page, size, totalElements, totalPages);
    }

    /** Slices an already filtered and sorted list. */
    public static <T> PageResult<T> slice(List<T> all, PageQuery query) {
        int from = (int) Math.min(query.offset(), all.size());
        int to = Math.min(from + query.size(), all.size());
        return of(all.subList(from, to), query.page(), query.size(), all.size());
    }
}
