package org.openpdf.layout;

import org.junit.jupiter.api.Test;
import org.openpdf.css.constants.IdentValue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.constants.IdentValue.LOWER_ROMAN;
import static org.openpdf.layout.CounterFunction.createCounterText;

class CounterFunctionTest {
    @Test
    void lowerLatin() {
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 0)).isEqualTo("");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 1)).isEqualTo("a");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 2)).isEqualTo("b");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 5)).isEqualTo("e");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 25)).isEqualTo("y");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 26)).isEqualTo("z");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 27)).isEqualTo("aa");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 28)).isEqualTo("ab");
        assertThat(createCounterText(IdentValue.LOWER_LATIN, 26*26 - 1)).isEqualTo("yy");
    }

    @Test
    void lowerRoman() {
        assertThat(createCounterText(LOWER_ROMAN, 0)).isEqualTo("");
        assertThat(createCounterText(LOWER_ROMAN, 1)).isEqualTo("i");
        assertThat(createCounterText(LOWER_ROMAN, 2)).isEqualTo("ii");
        assertThat(createCounterText(LOWER_ROMAN, 5)).isEqualTo("v");
        assertThat(createCounterText(LOWER_ROMAN, 10)).isEqualTo("x");
        assertThat(createCounterText(LOWER_ROMAN, 49)).isEqualTo("xlix");
        assertThat(createCounterText(LOWER_ROMAN, 50)).isEqualTo("l");
        assertThat(createCounterText(LOWER_ROMAN, 51)).isEqualTo("li");
        assertThat(createCounterText(LOWER_ROMAN, 99)).isEqualTo("xcix");
        assertThat(createCounterText(LOWER_ROMAN, 100)).isEqualTo("c");
        assertThat(createCounterText(LOWER_ROMAN, 104)).isEqualTo("civ");
    }
}