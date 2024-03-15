package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.lowagie.text.TextRenderingOptions;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class FontDetailsTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void convertToBytesBaseFontNullShouldThrowNpe() {
        assertThatNullPointerException().isThrownBy(() -> new FontDetails(null, null, null));
    }

    @Test
    void convertToBytesShouldExerciseSomeCode() throws IOException {
        String filename = "src/test/resources/fonts/jp/GenShinGothic-Normal.ttf";
        BaseFont baseFont = BaseFont.createFont(filename, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        FontDetails fontDetails = new FontDetails(null, null, baseFont);
        TextRenderingOptions options = new TextRenderingOptions();
        byte[] bytes = fontDetails.convertToBytes("hällö wörld", options);
        assertThat(bytes).hasSize(22);
        assertThat(fontDetails.isSubset()).isTrue();
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

}
