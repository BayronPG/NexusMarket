package com.nexusmarket.infrastructure.web;

import java.util.Set;

/**
 * Validates the sort field of the list endpoints against the fields they expose.
 *
 * <p>Fase 2.9: the adapters also reject unknown fields, but Spring translates
 * exceptions thrown inside a repository into data-access exceptions; validating
 * at the REST edge keeps the response a 400 (IllegalArgumentException is mapped
 * by the global handler).</p>
 */
public final class SortWhitelist {

    private SortWhitelist() {
    }

    /**
     * Fails when the sort field is not part of the endpoint contract.
     *
     * @param sort             sort expression received from the client
     * @param allowedSortFields fields accepted by the endpoint
     */
    public static void require(String sort, Set<String> allowedSortFields) {
        String field = sort == null || sort.isBlank()
                ? "id"
                : sort.split(",")[0].trim();
        if (!allowedSortFields.contains(field)) {
            throw new IllegalArgumentException("sort field not allowed: " + field);
        }
    }
}
