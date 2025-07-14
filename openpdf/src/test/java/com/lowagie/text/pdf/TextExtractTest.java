package com.lowagie.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextExtractTest {

    @Test
    void textExtractTest1() throws IOException {
        PdfReader reader = new PdfReader(TextExtractTest.class.getResourceAsStream("/identity-h.pdf"));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
        Assertions.assertEquals("Hello World", pdfTextExtractor.getTextFromPage(1));
    }

    @Test
    void textExtractTest2() throws IOException {
        PdfReader reader = new PdfReader(TextExtractTest.class.getResourceAsStream("/HelloWorldMeta.pdf"));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);
        Assertions.assertEquals("Hello World", pdfTextExtractor.getTextFromPage(1));
    }

    @Test
    void textCreateAndExtractTest2() throws IOException {
        LayoutProcessor.enableKernLiga();
        float fontSize = 12.0f;

        String testText = "กขน้ำตา ญูญูิ่ ก้กิ้";

        URL fontPath = TextExtractTest.class.getResource("/fonts/NotoSansThaiLooped/NotoSansThaiLooped-Regular.ttf");

        assertThat(fontPath).isNotNull();
        FontFactory.register(fontPath.toString(), "NotoSansThaiLooped");
        Font notoSansThaiLooped = FontFactory.getFont("NotoSansThaiLooped", BaseFont.IDENTITY_H, true, fontSize);
        notoSansThaiLooped.getBaseFont().setIncludeCidSet(false);

        ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();
        try (Document document = new Document()) {
            PdfWriter writer = PdfWriter.getInstance(document, pdfOutput);
            writer.setInitialLeading(16.0f);
            document.open();
            document.add(new Chunk(testText, notoSansThaiLooped));
        }
        LayoutProcessor.disable();

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfOutput.toByteArray()));
        PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(reader);

        // FileOutputStream test = new FileOutputStream("/tmp/output2.pdf");
        // pdfOutput.writeTo(test);

        // Ignore spaces in comparison
        Assertions.assertEquals("ก ข น ํ้ า ต า ญูญูิ่ ก้กิ้".replaceAll(" ", ""),
            pdfTextExtractor.getTextFromPage(1).replaceAll(" ", ""));
    }
}
