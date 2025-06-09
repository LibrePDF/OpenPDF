package org.openpdf.css.style.derived;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.style.derived.RectPropertySet.ALL_ZEROS;

class RectPropertySetTest {
    @Test
    void isAllZeros() {
        assertThat(ALL_ZEROS.isAllZeros()).isTrue();
        assertThat(new RectPropertySet(0, 0, 0, 0).isAllZeros()).isTrue();
        assertThat(new RectPropertySet(0, 0, 0, 1).isAllZeros()).isFalse();
        assertThat(new RectPropertySet(0, 0, 1, 0).isAllZeros()).isFalse();
        assertThat(new RectPropertySet(0, 1, 0, 0).isAllZeros()).isFalse();
        assertThat(new RectPropertySet(1, 0, 0, 0).isAllZeros()).isFalse();
    }

    @Test
    void hasNegativeValues() {
        assertThat(ALL_ZEROS.hasNegativeValues()).isFalse();
        assertThat(new RectPropertySet(0, 0, 0, 0).hasNegativeValues()).isFalse();
        assertThat(new RectPropertySet(1, 1, 1, 1).hasNegativeValues()).isFalse();
        assertThat(new RectPropertySet(0, 0, 0, -1).hasNegativeValues()).isTrue();
        assertThat(new RectPropertySet(0, 0, -1, 0).hasNegativeValues()).isTrue();
        assertThat(new RectPropertySet(0, -1, 0, 0).hasNegativeValues()).isTrue();
        assertThat(new RectPropertySet(-1, 0, 0, 0).hasNegativeValues()).isTrue();
    }
}