package com.nexusmarket.application.query;

/**
 * Framework independent pagination request.
 *
 * <p>INTERPRETATION: the application layer needs paging without depending on
 * Spring Data, so this type only carries the page, the size and the sort
 * expression ({@code field} or {@code field,asc|desc}).</p>
 *
 * <p>Invalid values raise {@link IllegalArgumentException}, which the REST edge
 * translates into 400.</p>
 */
public record PageQuery(int page, int size, String sort) {

    /** Page used when the client does not send one. */
    public static final int DEFAULT_PAGE = 0;

    /** Page size used when the client does not send one. */
    public static final int DEFAULT_SIZE = 20;

    /** Maximum accepted page size. */
    public static final int MAX_SIZE = 100;

    /** Sort used when the client does not send one. */
    public static final String DEFAULT_SORT = "id,asc";

    private static final String SORT_PATTERN = "[A-Za-z][A-Za-z0-9]*(,(?i:asc|desc))?";

    public PageQuery {
        if (page < DEFAULT_PAGE) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_SIZE) {
            throw new IllegalArgumentException("size must be between 1 and 100");
        }
        if (sort == null || sort.isBlank()) {
            sort = DEFAULT_SORT;
        }
        if (!sort.matches(SORT_PATTERN)) {
            throw new IllegalArgumentException("sort must be 'field' or 'field,asc|desc'");
        }
    }

    /** Builds a query applying the defaults to missing values. */
    public static PageQuery of(Integer page, Integer size, String sort) {
        return new PageQuery(page == null ? DEFAULT_PAGE : page,
                size == null ? DEFAULT_SIZE : size, sort);
    }

    /** Sort field without the direction. */
    public String field() {
        int comma = sort.indexOf(',');
        return comma < 0 ? sort : sort.substring(0, comma);
    }

    /** Whether the sort is descending. */
    public boolean descending() {
        return sort.toLowerCase().endsWith(",desc");
    }

    /** Index of the first element of the page. */
    public long offset() {
        return (long) page * size;
    }
}
