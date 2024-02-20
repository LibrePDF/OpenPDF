package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TrueTypeFontTest {

    @Test
    void testTrueTypeFontDefaultConstructor() {
        TrueTypeFont font = new TrueTypeFont();
        assertThat(font).isNotNull();
    }
}
