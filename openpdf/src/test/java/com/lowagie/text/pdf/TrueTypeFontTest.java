package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TrueTypeFontTest {

    @Test
    void testTrueTypeFontDefaultConstructor() {
        TrueTypeFont font = new TrueTypeFont();
        assertThat(font).isNotNull();
    }

    @Test
    void testGetTtcNameNoTtc() {
        assertThat(TrueTypeFont.getTTCName("font.ttf")).isEqualTo("font.ttf");
    }

    @Test
    void testGetTtcName() {
        assertThat(TrueTypeFont.getTTCName("font.ttc,123456")).isEqualTo("font.ttc");
    }
}
