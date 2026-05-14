package org.openpdf.pdf;

import org.openpdf.text.pdf.GlyphLayoutFontManager;
import org.openpdf.text.pdf.GlyphLayoutManager;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

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
        var fontResolver = new ITextFontResolver();

        loadFont(glyphLayoutManager, fontResolver, "Arimo-Regular.ttf", "fonts/Arimo-Regular.ttf");
        loadFont(glyphLayoutManager, fontResolver, "Arimo-Bold.ttf", "fonts/Arimo-Bold.ttf");

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

    private void loadFont(GlyphLayoutManager glyphLayoutManager, ITextFontResolver fontResolver,
            String fontName, String fontResourcePath)
            throws IOException, GlyphLayoutFontManager.FontLoadException {
        var fontUrl = this.getClass().getResource(fontResourcePath);
        Objects.requireNonNull(fontUrl, "Font not found: " + fontResourcePath);
        var fontStream = fontUrl.openStream();
        var font = glyphLayoutManager.loadFont(fontName, fontStream, 12.0f);
        fontStream.close();
        fontResolver.addFont(font.getBaseFont(), fontUrl.getFile(), null);
    }
}
