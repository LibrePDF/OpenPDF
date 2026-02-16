package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.openpdf.text.TextRenderingOptions;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class FontDetailsTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void convertToBytesBaseFontNullShouldThrowNpe() {
        assertThatNullPointerException().isThrownBy(() -> new FontDetails(null, null, null));
    }

    @Test
    void convertToBytesAwesomeShouldExerciseSomeCode() throws IOException {
        String fileName = "src/test/resources/fonts/font-awesome/fa-v4compatibility.ttf";
        BaseFont baseFont = BaseFont.createFont(fileName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        FontDetails fontDetails = new FontDetails(null, null, baseFont);
        TextRenderingOptions options = new TextRenderingOptions();
        String earthAmericas = "\uf0ac";
        byte[] bytes = fontDetails.convertToBytes(earthAmericas, options);
        assertThat(bytes).hasSize(2);
        assertThat(fontDetails.isSubset()).isTrue();
    }

    @Test
    void testFillerCMapHelveticaIsNull() throws IOException {
        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        FontDetails fontDetails = new FontDetails(null, null, baseFont);
        assertThat(fontDetails.getFillerCmap()).isNull();
    }

    @Test
    void testFillerCMapLiberationIsNotNull() throws IOException {
        String filename = "src/test/resources/fonts/liberation/LiberationSerif-Regular.ttf";
        BaseFont baseFont = BaseFont.createFont(filename, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        FontDetails fontDetails = new FontDetails(null, null, baseFont);
        assertThat(fontDetails.getFillerCmap()).isNotNull().isEmpty();
        fontDetails.putFillerCmap(1, new int[]{1, 2, 3});
        assertThat(fontDetails.getFillerCmap()).hasSize(1);
    }

    @Test
    void testIvsTextConversion() throws IOException {
        String filename = "src/test/resources/fonts/ivs/Hei_MSCS.ttf";
        BaseFont baseFont = BaseFont.createFont(filename, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        FontDetails fontDetails = new FontDetails(null, null, baseFont);
        TextRenderingOptions options = new TextRenderingOptions();
        options.setGlyphSubstitutionEnabled(false);
        String text = "㛇\uDB40\uDD01㛇\uDB40\uDD02";
        byte[] bytes = fontDetails.convertToBytes(text, options);

        assertThat(bytes).isNotNull().isNotEmpty();
        assertThat(fontDetails.longTag).isNotNull().isNotEmpty();
        // unicode kept
        assertThat(bytes).hasSize(4);
        // convert to 2 glyphs
        assertThat(fontDetails.longTag).hasSize(2);
    }

}
