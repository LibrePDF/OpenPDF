package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.lowagie.text.ExceptionConverter;
import java.io.IOException;
import org.apache.fop.fonts.truetype.TTFFile;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TTFCacheTest {

    @Test
    void whenGetTTFFileWithNotExistingFileAndNullTtuShouldThrowNpe() {
        assertThatNullPointerException().isThrownBy(() -> TTFCache.getTTFFile("test-TFFile-With-Null-Ttu", null));
    }

    @Test
    void whenGetTTFFileWithNullFileNameShouldThrowNpe() throws IOException {
        TrueTypeFontUnicode font = (TrueTypeFontUnicode) BaseFont
                .createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, false);
        assertThatNullPointerException().isThrownBy(() -> TTFCache.getTTFFile(null, font));
    }

    @Test
    void whenTTFCacheGetShouldEqualToTTFFileGet() throws IOException {
        // given
        TrueTypeFontUnicode font = (TrueTypeFontUnicode) BaseFont
                .createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, false);

        // when
        TTFFile ttfFile = TTFCache.getTTFFile("ViaodaLibre-Regular.ttf", font);

        // then
        assertThat(ttfFile).isNotNull();
        // load from cache
        TTFFile ttfFile1 = TTFCache.getTTFFile("ViaodaLibre-Regular.ttf", font);
        assertThat(ttfFile1).isEqualTo(ttfFile);
    }

    @Test
    void whenLoadTTFShouldThrowNpe() throws IOException {
        // given
        TrueTypeFontUnicode font = (TrueTypeFontUnicode) BaseFont
                .createFont("fonts/jaldi/Jaldi-Regular.otf", BaseFont.IDENTITY_H, false);
        font.cff = true;
        // when then
        Assertions.assertThatThrownBy(() -> TTFCache.getTTFFile("Jaldi-Regular.otf", font))
                .isInstanceOf(ExceptionConverter.class);
    }

}
