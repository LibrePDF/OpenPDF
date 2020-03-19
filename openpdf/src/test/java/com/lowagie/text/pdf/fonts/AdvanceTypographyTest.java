package com.lowagie.text.pdf.fonts;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.FopGlyphProcessor;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 *
 * @author  Gajendra kumar (raaz2.gajendra@gmail.com)
 */
public class AdvanceTypographyTest {

    /**
     * Without ghyph substitution out will be {660,666,676,1143,656,1130}, which is no correct
     * FopGlyphProcessor performs ghyph substitution to correct the output
     * @throws Exception
     */
    @Test
    public void testTypographySubstitution() throws Exception{
        char[] expectedOutput = {660,666,911,656,1130};
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                BaseFont.createFont("fonts/jaldi/Jaldi-Regular.ttf", BaseFont.IDENTITY_H, false)
                , "नमस्ते", "fonts/jaldi/Jaldi-Regular.ttf", new HashMap<>());
        String str = new String(processedContent, "UnicodeBigUnmarked");

        assertArrayEquals(expectedOutput,str.toCharArray());
    }

    /**
     * In some fonts combination of two characters can be represented by single glyph
     * This method tests above case.
     * @throws Exception
     */
    @Test
    public void testSubstitutionWithMerge() throws Exception{
        char[] expectedOutput = {254,278,390,314,331,376,254,285,278};
        byte[] processedContent = FopGlyphProcessor.convertToBytesWithGlyphs(
                BaseFont.createFont("fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", BaseFont.IDENTITY_H, false)
                , "instruction", "fonts/Viaoda_Libre/ViaodaLibre-Regular.ttf", new HashMap<>());
        String str = new String(processedContent, "UnicodeBigUnmarked");
        assertArrayEquals(expectedOutput,str.toCharArray());
    }

}
