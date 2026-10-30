package com.nexusmarket.application.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Metadata and slicing of the page result.
 *
 * <p>Fase 2.9 of docs/PLAN_API_WEB.md.</p>
 */
class PageResultTest {

    @Test
    void totalPagesIsDerivedFromTheTotalElements() {
        PageResult<Integer> page = PageResult.of(List.of(1, 2), 0, 2, 3);

        assertThat(page.totalPages()).isEqualTo(2);
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void anEmptyResultHasNoPages() {
        PageResult<Integer> page = PageResult.of(List.of(), 0, 20, 0);

        assertThat(page.totalPages()).isZero();
        assertThat(page.content()).isEmpty();
    }

    @Test
    void aNullContentBecomesAnEmptyList() {
        assertThat(new PageResult<Integer>(null, 0, 20, 0, 0).content()).isEmpty();
    }

    @Test
    void sliceTakesTheRequestedWindow() {
        PageResult<Integer> page = PageResult.slice(List.of(1, 2, 3, 4, 5),
                new PageQuery(1, 2, "id,asc"));

        assertThat(page.content()).containsExactly(3, 4);
        assertThat(page.totalElements()).isEqualTo(5);
        assertThat(page.totalPages()).isEqualTo(3);
    }

    @Test
    void sliceBeyondTheLastPageReturnsNoContentButKeepsTheTotal() {
        PageResult<Integer> page = PageResult.slice(List.of(1, 2, 3),
                new PageQuery(10, 5, "id,asc"));

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(3);
    }
}
