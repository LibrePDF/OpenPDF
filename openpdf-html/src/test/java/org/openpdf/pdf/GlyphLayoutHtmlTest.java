package org.openpdf.pdf;

import org.openpdf.text.pdf.GlyphLayoutManager;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileOutputStream;

public class GlyphLayoutHtmlTest {
    public static void main(String[] args) {
        var glyphLayoutTest = new GlyphLayoutHtmlTest();
        try {
            glyphLayoutTest.test();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void test() throws Exception {
        var html_filename = "GlyphLayoutHtmlTest.html";
        var inputStream = this.getClass().getResourceAsStream(html_filename);
        var documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        var document = documentBuilder.parse(inputStream);

        var glyphLayoutManager = new GlyphLayoutManager();
        var fontUrl = this.getClass().getResource("fonts/Arimo-Regular.ttf");
        var font = glyphLayoutManager.loadFont("Arimo-Regular.ttf", fontUrl.openStream(), 12.0f);
        var fontResolver = new ITextFontResolver();
        fontResolver.addFont(font.getBaseFont(), fontUrl.getFile(), null);

        var pdf_filename = "GlyphLayoutHtmlTest.pdf";
        try (var outputStream = new FileOutputStream(pdf_filename)) {
            var renderer = new ITextRenderer(fontResolver);
            renderer.setDocument(document);
            renderer.setGlyphLayoutManager(glyphLayoutManager);
            renderer.layout();
            renderer.createPDF(outputStream);
        }
        System.out.println("PDF created: " + pdf_filename);
    }
}
