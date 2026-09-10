package com.entri.modules.utils;

import com.entri.utils.PhoneNumberUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PhoneNumberUtilsTest {

    @Test
    void normalize_startingWith07_replacesLeadingZeroWith254() {
        assertThat(PhoneNumberUtils.normalize("0712345678")).isEqualTo("254712345678");
    }

    @Test
    void normalize_startingWith01_replacesLeadingZeroWith254() {
        assertThat(PhoneNumberUtils.normalize("0112345678")).isEqualTo("254112345678");
    }

    @Test
    void normalize_startingWithPlus254_stripsLeadingPlus() {
        assertThat(PhoneNumberUtils.normalize("+254712345678")).isEqualTo("254712345678");
    }

    @Test
    void normalize_alreadyNormalized_returnsUnchanged() {
        assertThat(PhoneNumberUtils.normalize("254712345678")).isEqualTo("254712345678");
    }

    @Test
    void normalize_unrecognizedFormat_returnsUnchanged() {
        assertThat(PhoneNumberUtils.normalize("447123456789")).isEqualTo("447123456789");
    }
}
