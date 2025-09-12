package com.lowagie.text.html;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests generating PDF from HTML with selected CCS style attributes (such as 'font-size', 'background',
 * 'background-color', 'color').
 */
class StylesTest {

    @Test
    void testBackgroundColor() throws Exception {
        List<Element> elements = htmlToPdf("stylesTest/backgroundColor.html", "target/Background Color.pdf");
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk1 = (Chunk) paragraph.get(0);
        Assertions.assertEquals(Color.BLUE, getBackgroundColor(chunk1));
        Chunk chunk2 = (Chunk) paragraph.get(2);
        Assertions.assertEquals(Color.BLUE, getBackgroundColor(chunk2));
        Chunk chunk3 = (Chunk) paragraph.get(4);
        Assertions.assertEquals(Color.BLUE, getBackgroundColor(chunk3));
        Chunk chunk4 = (Chunk) paragraph.get(6);
        Assertions.assertEquals(Color.BLUE, getBackgroundColor(chunk4));
    }

    private Color getBackgroundColor(Chunk chunk) {
        Object[] backgroundAttributes = (Object[]) chunk.getChunkAttributes().get(Chunk.BACKGROUND);
        if (backgroundAttributes != null && backgroundAttributes.length > 0
                && backgroundAttributes[0] instanceof Color) {
            return (Color) backgroundAttributes[0];
        }
        return null;
    }

    @Test
    void testFontColor() throws Exception {
        List<Element> elements = htmlToPdf("stylesTest/fontColor.html", "target/Font Color.pdf");
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk1 = (Chunk) paragraph.get(0);
        Assertions.assertEquals(Color.BLUE, chunk1.getFont().getColor());
        Chunk chunk2 = (Chunk) paragraph.get(2);
        Assertions.assertEquals(Color.BLUE, chunk2.getFont().getColor());
    }

    private List<Element> htmlToPdf(String htmlFileName, String pdfFileName) throws IOException {
        StyleSheet styleSheet = new StyleSheet();
        Map<String, Object> interfaceProps = new HashMap<>();
        try (InputStream inputStream = StylesTest.class.getClassLoader().getResourceAsStream(htmlFileName);
                OutputStream outputStream = Files.newOutputStream(Paths.get(pdfFileName))) {
            if (inputStream == null) {
                throw new IOException("InputStream could not be created");
            }
            List<Element> elements = HTMLWorker.parseToList(new InputStreamReader(inputStream), styleSheet,
                    interfaceProps);

            Document document = new Document();
            PdfWriter instance = PdfWriter.getInstance(document, outputStream);
            document.open();
            instance.getInfo().put(PdfName.CREATOR, new PdfString(Document.getVersion()));
            for (Element e : elements) {
                document.add(e);
            }
            document.close();
            return elements;
        }
    }

    @Test
    void testFontSize() throws Exception {
        List<Element> elements = htmlToPdf("stylesTest/fontSize.html", "target/Font Size.pdf");
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk1 = (Chunk) paragraph.get(0);
        float defaultFontSize = chunk1.getFont().getSize();
        Chunk chunk2 = (Chunk) paragraph.get(2);
        Assertions.assertEquals(8.0, chunk2.getFont().getSize());
        Chunk chunk3 = (Chunk) paragraph.get(4);
        Assertions.assertEquals(20.0, chunk3.getFont().getSize());
        Chunk chunk4 = (Chunk) paragraph.get(6);
        Assertions.assertEquals(1.5 * defaultFontSize, chunk4.getFont().getSize());
        Chunk chunk5 = (Chunk) paragraph.get(8);
        Assertions.assertEquals(0.5 * defaultFontSize, chunk5.getFont().getSize());
    }

    @Test
    void testNamedFontSize() throws Exception {
        List<Element> elements = htmlToPdf("stylesTest/fontSizeNamed.html", "target/Font Size Named.pdf");
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk1 = (Chunk) paragraph.get(0);
        Assertions.assertEquals(FontSize.XX_SMALL.getScale() * Markup.DEFAULT_FONT_SIZE, chunk1.getFont().getSize());
        Chunk chunk2 = (Chunk) paragraph.get(2);
        Assertions.assertEquals(FontSize.X_SMALL.getScale() * Markup.DEFAULT_FONT_SIZE, chunk2.getFont().getSize());
        Chunk chunk3 = (Chunk) paragraph.get(4);
        Assertions.assertEquals(FontSize.SMALL.getScale() * Markup.DEFAULT_FONT_SIZE, chunk3.getFont().getSize());
        Chunk chunk4 = (Chunk) paragraph.get(6);
        Assertions.assertEquals(FontSize.MEDIUM.getScale() * Markup.DEFAULT_FONT_SIZE, chunk4.getFont().getSize());
        Chunk chunk5 = (Chunk) paragraph.get(8);
        Assertions.assertEquals(FontSize.LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk5.getFont().getSize());
        Chunk chunk6 = (Chunk) paragraph.get(10);
        Assertions.assertEquals(FontSize.X_LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk6.getFont().getSize());
        Chunk chunk7 = (Chunk) paragraph.get(12);
        Assertions.assertEquals(FontSize.XX_LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk7.getFont().getSize());
        Chunk chunk8 = (Chunk) paragraph.get(14);
        Assertions.assertEquals(FontSize.XXX_LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk8.getFont().getSize());

        Chunk chunk9 = (Chunk) paragraph.get(16);
        Assertions.assertEquals(FontSize.SMALLER.getScale() * 20f, chunk9.getFont().getSize());
        Chunk chunk10 = (Chunk) paragraph.get(18);
        Assertions.assertEquals(FontSize.LARGER.getScale() * 20f, chunk10.getFont().getSize());
    }

    @Test
    void testLineHeightPercentage() throws Exception {
        List<Element> elements = htmlToPdf("stylesTest/lineHeightPercentage.html", "target/Line Height Percentage.pdf");
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk = (Chunk) paragraph.get(0);
        float fontSize;
        if (chunk.getFont() != null) {
            fontSize = chunk.getFont().getSize();
        } else {
            fontSize = Font.DEFAULTSIZE;
        }
        float expectedMultiplier = 1.15f; // derived from <span style="line-height:115%"> in lineHeightPercentage.html
        float expectedTotalLeading = fontSize * expectedMultiplier;
        float totalLeading = paragraph.getTotalLeading();

        Assertions.assertEquals(expectedTotalLeading, totalLeading, 0.1f,
                "Total leading should be ~fontSize * " + expectedMultiplier);
    }

    @Test
    void testDefaultLineHeight() throws Exception {
        List<Element> elements = htmlToPdf("stylesTest/lineHeightDefault.html", "target/Line Height Default.pdf");
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk = (Chunk) paragraph.get(0);
        float fontSize;
        if (chunk.getFont() != null) {
            fontSize = chunk.getFont().getSize();
        } else {
            fontSize = Font.DEFAULTSIZE;
        }
        float expectedMultiplier = 1.5f;
        float expectedTotalLeading = fontSize * expectedMultiplier;
        float totalLeading = paragraph.getTotalLeading();

        Assertions.assertEquals(expectedTotalLeading, totalLeading, 0.1f,
                "Total leading should be ~fontSize * " + expectedMultiplier);
    }

}
