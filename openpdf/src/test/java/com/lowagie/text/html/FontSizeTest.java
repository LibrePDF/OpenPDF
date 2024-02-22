package com.lowagie.text.html;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontSizeTest {

    @Test
    public void testFontSize() throws Exception {
        StringReader reader = new StringReader(
            "<span>Text</span>" +
                "<span style=\"font-size:8.0pt\">Text 8.0pt</span>"
                + "<span style=\"font-size:20px\">Text 20px</span>"
                + "<span style=\"font-size:1.5em\">Text 1.5em</span>"
                + "<span style=\"font-size:50%\">Text 50%</span>");
        StyleSheet styleSheet = new StyleSheet();
        Map<String, Object> interfaceProps = new HashMap<>();
        List<Element> elements = HTMLWorker.parseToList(reader, styleSheet, interfaceProps);

        Document document = new Document();
        PdfWriter instance = PdfWriter.getInstance(document, new FileOutputStream("target/Font Size.pdf"));
        document.open();
        instance.getInfo().put(PdfName.CREATOR, new PdfString(Document.getVersion()));
        for (Element e : elements) {
            document.add(e);
        }
        document.close();
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk1 = (Chunk) paragraph.get(0);
        float defaultFontSize = chunk1.getFont().getSize();
        Chunk chunk2 = (Chunk) paragraph.get(1);
        Assertions.assertEquals(8.0, chunk2.getFont().getSize());
        Chunk chunk3 = (Chunk) paragraph.get(2);
        Assertions.assertEquals(20.0, chunk3.getFont().getSize());
        Chunk chunk4 = (Chunk) paragraph.get(3);
        Assertions.assertEquals(1.5 * defaultFontSize, chunk4.getFont().getSize());
        Chunk chunk5 = (Chunk) paragraph.get(4);
        Assertions.assertEquals(0.5 * defaultFontSize, chunk5.getFont().getSize());
    }

    @Test
    public void testNamedFontSize() throws Exception {
        StringReader reader = new StringReader(
            "<span style=\"font-size:20pt\">" +
                "<span style=\"font-size:xx-small\">Text xx-small</span><br/>" +
                "<span style=\"font-size:x-small\">Text x-small</span><br/>" +
                "<span style=\"font-size:small\">Text small</span><br/>" +
                "<span style=\"font-size:medium\">Text medium</span><br/>" +
                "<span style=\"font-size:large\">Text large</span><br/>" +
                "<span style=\"font-size:x-large\">Text x-large</span><br/>" +
                "<span style=\"font-size:xx-large\">Text xx-large</span><br/>" +
                "<span style=\"font-size:xxx-large\">Text xxx-large</span><br/>" +
                "</span>" +

                "<span style=\"font-size:20pt\">" +
                "<span style=\"font-size:smaller\">Text smaller</span><br/>" +
                "<span style=\"font-size:larger\">Text larger</span><br/>" +
                "</span>"
        );
        StyleSheet styleSheet = new StyleSheet();
        Map<String, Object> interfaceProps = new HashMap<>();
        List<Element> elements = HTMLWorker.parseToList(reader, styleSheet, interfaceProps);

        Document document = new Document();
        PdfWriter instance = PdfWriter.getInstance(document, new FileOutputStream("target/Font Size Named.pdf"));
        document.open();
        instance.getInfo().put(PdfName.CREATOR, new PdfString(Document.getVersion()));
        for (Element e : elements) {
            document.add(e);
        }
        document.close();

        int i = 0;
        Paragraph paragraph = (Paragraph) elements.get(0);
        Chunk chunk1 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.XX_SMALL.getScale() * Markup.DEFAULT_FONT_SIZE, chunk1.getFont().getSize());
        Chunk chunk2 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.X_SMALL.getScale() * Markup.DEFAULT_FONT_SIZE, chunk2.getFont().getSize());
        Chunk chunk3 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.SMALL.getScale() * Markup.DEFAULT_FONT_SIZE, chunk3.getFont().getSize());
        Chunk chunk4 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.MEDIUM.getScale() * Markup.DEFAULT_FONT_SIZE, chunk4.getFont().getSize());
        Chunk chunk5 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk5.getFont().getSize());
        Chunk chunk6 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.X_LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk6.getFont().getSize());
        Chunk chunk7 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.XX_LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk7.getFont().getSize());
        Chunk chunk8 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.XXX_LARGE.getScale() * Markup.DEFAULT_FONT_SIZE, chunk8.getFont().getSize());

        Chunk chunk9 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.SMALLER.getScale() * 20f, chunk9.getFont().getSize());
        Chunk chunk10 = (Chunk) paragraph.get((i++) * 2);
        Assertions.assertEquals(FontSize.LARGER.getScale() * 20f, chunk10.getFont().getSize());
    }
}
