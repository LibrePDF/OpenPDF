package com.lowagie.text.factories;

import static com.lowagie.text.factories.RomanAlphabetFactory.getString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

// Deprecated: use org.openpdf package (openpdf-core-modern)
@Deprecated
class RomanAlphabetFactoryTest {

    @Test
    void shouldGetRomanString() {
        for (int i = 1; i < 32000; i++) {
            assertNotNull(getString(i));
        }
    }

}