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
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FontSizeTest {

    @Test
    void testFontSize() throws Exception {
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
        PdfWriter instance = PdfWriter.getInstance(document, Files.newOutputStream(Paths.get("target/Font Size.pdf")));
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
}
