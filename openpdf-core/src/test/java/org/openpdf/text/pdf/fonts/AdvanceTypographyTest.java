package org.openpdf.text.pdf.fonts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.FopGlyphProcessor;
import org.openpdf.text.pdf.LayoutProcessor;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Gajendra kumar (raaz2.gajendra@gmail.com)
 */
class AdvanceTypographyTest {

    /**
     * In some fonts combination of two characters can be represented by single glyph This method tests above case.
     *
     * @throws Exception - UnsupportedEncodingException by the convertToBytesWithGlyphs method
     */
    @Test
    void testSubstitutionWithMerge() throws Exception {
        char[] expectedOutput = {254, 278, 390, 314, 331, 376, 254, 285, 278};
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                BaseFont.createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, false),
                "instruction", "fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");
        assertArrayEquals(expectedOutput, str.toCharArray());
    }

    @Test
    void testSubstitutionWithMergeWithLayoutProcessorEnabled() throws Exception {
        LayoutProcessor.enable();
        char[] expectedOutput = {254, 278, 390, 314, 331, 376, 254, 285, 278};
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                BaseFont.createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, true, false,
                        null,
                        null),
                "instruction", "fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");
        assertArrayEquals(expectedOutput, str.toCharArray());
        LayoutProcessor.disable();
    }

    /**
     * Test fonts loaded externally and passed as byte array to BaseFont, Fop should be able to resolve these fonts
     *
     * @throws Exception a DocumentException or an IOException thrown by BaseFont.createFont
     */
    @Test
    void testInMemoryFonts() throws Exception {
        char[] expectedOutput = {254, 278, 390, 314, 331, 376, 254, 285, 278};
        BaseFont font = BaseFont.createFont("ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED, true,
                getTestFontByte(), null, false, false);
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                font, "instruction", "Viaoda Libre", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");
        assertArrayEquals(expectedOutput, str.toCharArray());
    }

    @Disabled("This test is failing, need to investigate. @YOSHIDA may know the reason."
            + "Should work, when GH-591 #592 is fixed.")
    @Test
    void testSurrogatePair() throws Exception {

        BaseFont baseFont = BaseFont.createFont("fonts/jp/GenShinGothic-Normal.ttf", BaseFont.IDENTITY_H, false);

        char[] expectedOutput = {17369};
        // http://en.glyphwiki.org/wiki/u20bb7
        String text = "\uD842\uDFB7";
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                baseFont, text, "fonts/jp/GenShinGothic-Normal.ttf", new HashMap<>(), "dflt");
        String str = new String(processedContent, "UnicodeBigUnmarked");
        char[] actual = str.toCharArray();
        assertArrayEquals(expectedOutput, actual);
    }

    private byte[] getTestFontByte() throws IOException {
        InputStream stream = BaseFont.getResourceStream("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", null);
        assertThat(stream).isNotNull();
        return IOUtils.toByteArray(stream);
    }
}
