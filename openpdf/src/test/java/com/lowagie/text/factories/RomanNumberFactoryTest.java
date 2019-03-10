package com.lowagie.text.factories;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.lowagie.text.factories.RomanNumberFactory.getString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

    @ParameterizedTest
    @MethodSource("numeralTestProvider")
    void shouldConvertRomanNumeralRepresentation(int input, String expected) {
        assertAll(
                () -> assertThat(getString(input), is(expected)),
                () -> assertThat(getString(input, false), is(expected.toUpperCase())));
    }

    static Stream<Arguments> numeralTestProvider() {
        return Stream.of(
                arguments(0, ""),
                arguments(1, "i"),
                arguments(2, "ii"),
                arguments(3, "iii"),
                arguments(4, "iv"),
                arguments(5, "v"),
                arguments(6, "vi"),
                arguments(7, "vii"),
                arguments(8, "viii"),
                arguments(9, "ix"),
                arguments(10, "x"),
                arguments(20, "xx"),
                arguments(30, "xxx"),
                arguments(40, "xl"),
                arguments(50, "l"),
                arguments(90, "xc"),
                arguments(100, "c"),
                arguments(400, "cd"),
                arguments(500, "d"),
                arguments(900, "cm"),
                arguments(1000, "m")
        );
    }
}
