package org.openpdf.pdf;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CJKFontResolverTest {
    private final CJKFontResolver resolver = new CJKFontResolver();

    @Test
    void loadsChinaJapanKoreanFonts() {
        Map<String, FontFamily> cjkFonts = resolver.loadFonts();
        assertThat(cjkFonts).hasSizeBetween(10, 1000);
        assertThat(cjkFonts).containsKeys("HYGoThic-Medium-V", "HYSMyeongJoStd-Medium-H", "MSung-Light-H");
        assertThat(cjkFonts.get("HYGoThic-Medium-V").getName()).isEqualTo("HYGoThic-Medium-V");
        assertThat(cjkFonts.get("HYGoThic-Medium-V").getFontDescriptions()).hasSize(4);
        assertThat(cjkFonts.get("HYGoThic-Medium-V").getFontDescriptions().get(0)).hasToString("Font HYGoThic-Medium:400");
    }

    @Test
    void loadsDefaultFontsAsWell() {
        Map<String, FontFamily> cjkFonts = resolver.loadFonts();
        assertThat(cjkFonts).hasSizeBetween(10, 1000);
        assertThat(cjkFonts).containsKeys("Courier", "Helvetica", "Serif", "TimesRoman");
        assertThat(cjkFonts.get("Courier").getName()).isEqualTo("Courier");
        assertThat(cjkFonts.get("Courier").getFontDescriptions()).hasSize(4);
        assertThat(cjkFonts.get("Courier").getFontDescriptions().get(0)).hasToString("Font Courier-Oblique:400");
    }
}