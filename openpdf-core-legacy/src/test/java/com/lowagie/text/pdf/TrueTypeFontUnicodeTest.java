package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.lowagie.text.DocumentException;
import org.junit.jupiter.api.Test;

class TrueTypeFontUnicodeTest {

    // Test that an Exception will be thrown when the font is not a TTF
    @Test
    void test() {
        assertThatThrownBy(
                () -> new TrueTypeFontUnicode("notReallyAFont", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, null, true))
                .isInstanceOf(DocumentException.class)
                .hasMessage("notReallyAFont  is not a TTF font file.");
    }

}
