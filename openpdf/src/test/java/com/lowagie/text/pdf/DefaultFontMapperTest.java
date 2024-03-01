package com.lowagie.text.pdf;

import org.junit.Before;
import org.junit.Test;


import java.awt.Font;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class DefaultFontMapperTest {

    private DefaultFontMapper fontMapper;

    @Before
    public void setUp() {
        fontMapper = new DefaultFontMapper();
    }

    @Test
    public void testAwtToPdf() {
        Font font = new Font("Arial", Font.PLAIN, 12);
        BaseFont baseFont = fontMapper.awtToPdf(font);
        assertNotNull(baseFont);
    }

    @Test
    public void testPdfToAwt() throws Exception {
        BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, false);
        Font font = fontMapper.pdfToAwt(baseFont, 12);
        assertNotNull(font);
        assertEquals("Helvetica", font.getName());
        assertEquals(Font.PLAIN, font.getStyle());
        assertEquals(12, font.getSize());
    }


    @Test
    public void testPutName() {
        String fontName = "Arial";
        DefaultFontMapper.BaseFontParameters parameters = new DefaultFontMapper.BaseFontParameters(fontName);
        fontMapper.putName(fontName, parameters);

        assertEquals(parameters, fontMapper.getMapper().get(fontName));
    }

    @Test
    public void testPutAlias() {
        String alias = "alias";
        String fontName = "Arial";
        fontMapper.putAlias(alias, fontName);

        assertEquals(fontName, fontMapper.getAliases().get(alias));
    }

}
