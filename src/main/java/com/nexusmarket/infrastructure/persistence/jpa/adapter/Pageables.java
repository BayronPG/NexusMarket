package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import com.nexusmarket.application.query.PageQuery;
import com.nexusmarket.application.query.PageResult;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Translation between the application pagination types and Spring Data.
 *
 * <p>Fase 2.9: the adapters keep the framework out of {@code application}; this
 * helper is the only place that knows about {@link Pageable}.</p>
 */
final class Pageables {

    private Pageables() {
    }

    /** Builds a Spring Data pageable, rejecting sort fields outside the whitelist. */
    static Pageable of(PageQuery query, Set<String> allowedSortFields) {
        if (!allowedSortFields.contains(query.field())) {
            throw new IllegalArgumentException("sort field not allowed: " + query.field());
        }
        Sort sort = Sort.by(query.descending() ? Sort.Direction.DESC : Sort.Direction.ASC,
                query.field());
        return PageRequest.of(query.page(), query.size(), sort);
    }

    /** Maps a Spring Data page into the application page. */
    static <E, D> PageResult<D> result(Page<E> page, Function<E, D> mapper) {
        List<D> content = page.getContent().stream().map(mapper).toList();
        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
