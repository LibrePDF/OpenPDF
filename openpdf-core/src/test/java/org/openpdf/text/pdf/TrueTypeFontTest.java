package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
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

    /**
     * Regression test for issue #1553. PR #1482 added a fallback in
     * {@link TrueTypeFont#getMetricsTT(int)} that called
     * {@code cmap05.get(c)} where {@code cmap05} is a {@code Map<String, int[]>}
     * but {@code c} is an {@code int}. The lookup was therefore guaranteed to
     * miss and was flagged by SpotBugs as {@code GC_UNRELATED_TYPES}. The fix
     * removes the dead fallback. This test exercises {@code getMetricsTT}
     * on a TrueType font that loads a format-14 (IVS) {@code cmap05} table
     * to ensure single-character lookups still resolve via the standard
     * cmap subtables.
     */
    @Test
    void getMetricsTTResolvesFromStandardCmapWhenCmap05Present() throws IOException {
        String filename = "src/test/resources/fonts/ivs/Hei_MSCS.ttf";
        BaseFont baseFont = BaseFont.createFont(filename, BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        assertThat(baseFont).isInstanceOf(TrueTypeFontUnicode.class);
        TrueTypeFont font = (TrueTypeFont) baseFont;
        // CJK char '㛇' (U+36C7) is supported by the test font and is reachable
        // via the standard cmap; the buggy cmap05 fallback (with String keys)
        // could never satisfy this lookup.
        int[] metrics = font.getMetricsTT(0x36C7);
        assertThat(metrics).isNotNull().hasSize(2);
        assertThat(metrics[1]).isPositive();
    }
}
