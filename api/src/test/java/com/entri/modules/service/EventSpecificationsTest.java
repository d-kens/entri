package com.entri.modules.service;

import com.entri.modules.events.entity.Event;
import com.entri.modules.events.specification.EventSpecifications;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EventSpecificationsTest {

    @Mock Root<Event> root;
    @Mock CriteriaQuery<?> query;
    @Mock CriteriaBuilder cb;
    @Mock Path<Instant> startTimePath;
    @Mock Predicate predicate;

    // ── startFrom ─────────────────────────────────────────────────────

    @Test
    void startFrom_null_returnsNullPredicate() {
        Specification<Event> spec = EventSpecifications.startFrom(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNull();
        verifyNoInteractions(root, cb);
    }

    @Test
    void startFrom_validDate_callsGreaterThanOrEqualTo() {
        String isoDate = "2026-06-14T00:00:00.000Z";
        Instant from = Instant.parse(isoDate);

        when(root.<Instant>get("startTime")).thenReturn(startTimePath);
        when(cb.greaterThanOrEqualTo(startTimePath, from)).thenReturn(predicate);

        Specification<Event> spec = EventSpecifications.startFrom(isoDate);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(predicate);
        verify(root).get("startTime");
        verify(cb).greaterThanOrEqualTo(startTimePath, from);
    }

    @Test
    void startFrom_parsesInstantCorrectly() {
        String isoDate = "2026-01-01T00:00:00.000Z";
        Instant expected = Instant.parse(isoDate);

        when(root.<Instant>get("startTime")).thenReturn(startTimePath);
        when(cb.greaterThanOrEqualTo(startTimePath, expected)).thenReturn(predicate);

        Specification<Event> spec = EventSpecifications.startFrom(isoDate);
        spec.toPredicate(root, query, cb);

        verify(cb).greaterThanOrEqualTo(startTimePath, expected);
    }

    @Test
    void startFrom_invalidDate_throwsDateTimeParseException() {
        assertThatThrownBy(() -> EventSpecifications.startFrom("not-a-date"))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }

    // ── startTo ───────────────────────────────────────────────────────

    @Test
    void startTo_null_returnsNullPredicate() {
        Specification<Event> spec = EventSpecifications.startTo(null);

        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isNull();
        verifyNoInteractions(root, cb);
    }

    @Test
    void startTo_validDate_callsLessThanOrEqualTo() {
        String isoDate = "2026-06-17T23:59:59.999Z";
        Instant to = Instant.parse(isoDate);

        when(root.<Instant>get("startTime")).thenReturn(startTimePath);
        when(cb.lessThanOrEqualTo(startTimePath, to)).thenReturn(predicate);

        Specification<Event> spec = EventSpecifications.startTo(isoDate);
        Predicate result = spec.toPredicate(root, query, cb);

        assertThat(result).isEqualTo(predicate);
        verify(root).get("startTime");
        verify(cb).lessThanOrEqualTo(startTimePath, to);
    }

    @Test
    void startTo_parsesInstantCorrectly() {
        String isoDate = "2026-12-31T23:59:59.999Z";
        Instant expected = Instant.parse(isoDate);

        when(root.<Instant>get("startTime")).thenReturn(startTimePath);
        when(cb.lessThanOrEqualTo(startTimePath, expected)).thenReturn(predicate);

        Specification<Event> spec = EventSpecifications.startTo(isoDate);
        spec.toPredicate(root, query, cb);

        verify(cb).lessThanOrEqualTo(startTimePath, expected);
    }

    @Test
    void startTo_invalidDate_throwsDateTimeParseException() {
        assertThatThrownBy(() -> EventSpecifications.startTo("not-a-date"))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
    }

    // ── both null — safe to combine ───────────────────────────────────

    @Test
    void bothNull_combinedPredicatesAreNull() {
        Predicate from = EventSpecifications.startFrom(null).toPredicate(root, query, cb);
        Predicate to   = EventSpecifications.startTo(null).toPredicate(root, query, cb);

        assertThat(from).isNull();
        assertThat(to).isNull();
        verifyNoInteractions(root, cb);
    }
}
