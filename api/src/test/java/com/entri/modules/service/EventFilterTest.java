package com.entri.modules.service;

import com.entri.modules.events.dto.EventFilter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EventFilterTest {

    // ── startFrom sanitization ────────────────────────────────────────

    @Test
    void startFrom_null_isKeptNull() {
        assertThat(filter(null, null).startFrom()).isNull();
    }

    @Test
    void startFrom_blank_isNullified() {
        assertThat(filter("   ", null).startFrom()).isNull();
    }

    @Test
    void startFrom_empty_isNullified() {
        assertThat(filter("", null).startFrom()).isNull();
    }

    @Test
    void startFrom_validIsoString_isTrimmed() {
        assertThat(filter("  2026-06-14T00:00:00.000Z  ", null).startFrom())
                .isEqualTo("2026-06-14T00:00:00.000Z");
    }

    @Test
    void startFrom_validIsoString_isPreserved() {
        assertThat(filter("2026-06-14T00:00:00.000Z", null).startFrom())
                .isEqualTo("2026-06-14T00:00:00.000Z");
    }

    // ── startTo sanitization ──────────────────────────────────────────

    @Test
    void startTo_null_isKeptNull() {
        assertThat(filter(null, null).startTo()).isNull();
    }

    @Test
    void startTo_blank_isNullified() {
        assertThat(filter(null, "   ").startTo()).isNull();
    }

    @Test
    void startTo_empty_isNullified() {
        assertThat(filter(null, "").startTo()).isNull();
    }

    @Test
    void startTo_validIsoString_isTrimmed() {
        assertThat(filter(null, "  2026-06-17T23:59:59.999Z  ").startTo())
                .isEqualTo("2026-06-17T23:59:59.999Z");
    }

    @Test
    void startTo_validIsoString_isPreserved() {
        assertThat(filter(null, "2026-06-17T23:59:59.999Z").startTo())
                .isEqualTo("2026-06-17T23:59:59.999Z");
    }

    // ── defaults are unaffected ───────────────────────────────────────

    @Test
    void defaults_areUnaffected_whenOnlyDateFieldsProvided() {
        EventFilter f = filter("2026-06-14T00:00:00.000Z", "2026-06-17T23:59:59.999Z");
        assertThat(f.page()).isZero();
        assertThat(f.size()).isEqualTo(10);
        assertThat(f.sortDirection()).isEqualTo("ASC");
    }

    // ── helpers ───────────────────────────────────────────────────────

    private EventFilter filter(String startFrom, String startTo) {
        return new EventFilter(null, null, null, null, null, null, startFrom, startTo);
    }
}
