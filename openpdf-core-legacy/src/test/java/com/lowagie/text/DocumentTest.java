package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void testThatVersionIsCorrect() {
        // Given
        String versionInCode = Document.getVersion();

        // Then
        assertThat(versionInCode)
                .as("Version number in code %s is not correct.", versionInCode)
                .matches("^OpenPDF \\d+\\.\\d+\\.\\d+(-SNAPSHOT)?$");
    }

    @Test
    void whenSetFootsToValueShouldSetObject() {
        // Given
        try (Document document = new Document()) {
            HeaderFooter footer = new HeaderFooter(new Phrase("Footer"), false);

            // When
            document.setFooter(footer);

            // Then
            assertThat(document.footer).isEqualTo(footer);
        }
    }

    @Test
    void whenResetFooterShouldSetNull() {
        // Given
        try (Document document = new Document()) {
            HeaderFooter footer = new HeaderFooter(new Phrase("Footer"), false);
            document.setFooter(footer);

            // When
            document.resetFooter();

            // Then
            assertThat(document.footer).isNull();
        }
    }

    @Test
    void documentSmokeTest() throws IOException {
        // given
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String string = "Latin: äöüÄÖÜß, symbol: ▲";
        // creates a PDF with the string
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, output);
        document.getTextRenderingOptions().setGlyphSubstitutionEnabled(false);
        document.open();
        document.add(new Paragraph(string));
        document.close();
        // extracts the text from the PDF
        byte[] pdfBytes = output.toByteArray();
        PdfTextExtractor extractor = new PdfTextExtractor(new PdfReader(pdfBytes));
        // then
        assertThat(extractor.getTextFromPage(1)).isEqualTo(string);
    }

}
