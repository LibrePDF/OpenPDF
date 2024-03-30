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
}
