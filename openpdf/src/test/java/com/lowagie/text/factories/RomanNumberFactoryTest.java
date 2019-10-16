package com.lowagie.text.factories;

import static com.lowagie.text.factories.RomanNumberFactory.getString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class RomanNumberFactoryTest {

    @Test
    void shouldGetRomanNumberString() {
        for (int i = 1; i < 2000; i++) {
            assertNotNull(getString(i));
        }
    }

    /**
     * Tests a few random roman numerals
     */
    @Test
    void shouldGetRomanNumeralRepresentation() {
        assertEquals("lvi", getString(56));
        assertEquals("mmcmxcix", getString(2999));
        assertEquals("mmm", getString(3000));
    }

    static Object[][] numeralTestProvider() {
        return new Object[][]{
                {0, ""},
                {1, "i"},
                {2, "ii"},
                {3, "iii"},
                {4, "iv"},
                {5, "v"},
                {6, "vi"},
                {7, "vii"},
                {8, "viii"},
                {9, "ix"},
                {10, "x"},
                {20, "xx"},
                {30, "xxx"},
                {40, "xl"},
                {50, "l"},
                {90, "xc"},
                {100, "c"},
                {400, "cd"},
                {500, "d"},
                {900, "cm"},
                {1000, "m"}
        };
    }

    @ParameterizedTest
    @MethodSource("numeralTestProvider")
    void shouldConvertRomanNumeralRepresentation(int input, String expected) {
        assertThat(getString(input), is(expected));
        assertThat(getString(input, false), is(expected.toUpperCase()));
    }
}
