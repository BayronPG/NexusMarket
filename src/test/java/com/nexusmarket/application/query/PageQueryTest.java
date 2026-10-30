package com.nexusmarket.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Limits and defaults of the pagination request.
 *
 * <p>Fase 2.9 of docs/PLAN_API_WEB.md.</p>
 */
class PageQueryTest {

    @Test
    void missingValuesFallBackToTheDefaults() {
        PageQuery query = PageQuery.of(null, null, null);

        assertThat(query.page()).isZero();
        assertThat(query.size()).isEqualTo(20);
        assertThat(query.sort()).isEqualTo("id,asc");
    }

    @Test
    void blankSortFallsBackToTheDefault() {
        assertThat(PageQuery.of(0, 10, "  ").sort()).isEqualTo("id,asc");
    }

    @Test
    void negativePageIsRejected() {
        assertThatThrownBy(() -> new PageQuery(-1, 10, "id,asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }

    @Test
    void sizeOutOfRangeIsRejected() {
        assertThatThrownBy(() -> new PageQuery(0, 0, "id,asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
        assertThatThrownBy(() -> new PageQuery(0, 101, "id,asc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }

    @Test
    void malformedSortIsRejected() {
        assertThatThrownBy(() -> new PageQuery(0, 10, "id;drop table"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sort");
        assertThatThrownBy(() -> new PageQuery(0, 10, "id,up"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sort");
    }

    @Test
    void fieldDirectionAndOffsetAreDerivedFromSort() {
        PageQuery descending = new PageQuery(2, 10, "status,desc");

        assertThat(descending.field()).isEqualTo("status");
        assertThat(descending.descending()).isTrue();
        assertThat(descending.offset()).isEqualTo(20);
        assertThat(new PageQuery(0, 5, "id").descending()).isFalse();
    }
}
