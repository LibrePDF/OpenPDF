package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class BaseFontTest {

    @Test
    void testGetAllNameEntries() throws IOException {
        // given
        byte[] bytes = getTestFontBytes();
        // when
        String[][] allNameEntries = BaseFont.getAllNameEntries("ViaodaLibre-Regular.ttf", "UTF-8", bytes);
        // then
        assertThat(allNameEntries).hasDimensions(11, 5);
    }

    @Test
    void testGetFullFontName() throws IOException {
        // given
        byte[] bytes = getTestFontBytes();
        // when
        String[][] fullFontName = BaseFont.getFullFontName("ViaodaLibre-Regular.ttf", "UTF-8", bytes);
        // then
        assertThat(fullFontName).hasDimensions(1, 4)
                .contains(new String[]{"3", "1", "1033", "Viaoda Libre Regular"}, atIndex(0));
    }

    // test getDescent()
    @Test
    void testGetDescent() throws IOException {
        // when
        BaseFont font = BaseFont.createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, false);
        // then
        assertThat(font.getDescent("byte")).isEqualTo(-264);
    }

    private byte[] getTestFontBytes() throws IOException {
        InputStream resourceStream = BaseFont.getResourceStream("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", null);
        assertThat(resourceStream).as("Font could not be loaded").isNotNull();
        return IOUtils.toByteArray(resourceStream);
    }
}
