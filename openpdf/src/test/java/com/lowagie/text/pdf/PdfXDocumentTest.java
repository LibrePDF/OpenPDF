package com.lowagie.text.pdf;

import java.awt.color.ICC_Profile;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.lowagie.text.Document;
import java.io.OutputStream;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test that creates a PDF/X-1a:2001 conforming document using OpenPDF
 * and verifies the presence of key structural PDF/X markers.
 * <p>
 * This test embeds:
 * <ul>
 *   <li>A color profile (ISOcoated_v2_300_eci.icc)</li>
 *   <li>An embedded TrueType font (LiberationSerif-Regular.ttf)</li>
 *   <li>A CMYK JPEG image</li>
 *   <li>PDF metadata including OutputIntent and Trapped</li>
 * </ul>
 * The test checks for conformance-related strings in the resulting PDF output.
 * It does not perform full ISO validation (e.g. with VeraPDF).
 */
public class PdfXDocumentTest {

    @Test
    public void testCreateAndVerifyMinimalPdfX1aMarkers() throws Exception {
        // Step 1: Load ICC profile
        InputStream iccStream = getClass().getResourceAsStream("/icc/ISOcoated_v2_300_eci.icc");
        byte[] iccBytes = iccStream.readAllBytes();
        ICC_Profile icc = ICC_Profile.getInstance(iccBytes);

        // Step 2: Load LiberationSerif-Regular font
        BaseFont baseFont = BaseFont.createFont("LiberationSerif-Regular.ttf", BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED, true, getLiberationFontByte(), null);
        Font font = new Font(baseFont, 12);

        // Step 3: Create PDF
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setPDFXConformance(PdfWriter.PDFX1A2001);
        writer.setPdfVersion(PdfWriter.VERSION_1_3);
        document.open();

        PdfOutputIntent outputIntent = new PdfOutputIntent(
                writer,
                "ISO Coated v2 300% (ECI)",
                "ISO Coated v2 300%",
                "http://www.color.org",
                "ISOcoated_v2_300_eci",
                icc
        );
        outputIntent.setOutputIntentSubtype(new PdfName("GTS_PDFX"));

        PdfDocument pdfDoc = writer.getPdfDocument();
        pdfDoc.addOutputIntent(outputIntent);

        pdfDoc.add(new Paragraph("This is a PDF/X-1a:2001 document with embedded LiberationSerif font.", font));

        Image cmykImage = Image.getInstance(getCmykImageBytes());
        cmykImage.scaleToFit(200, 100);
        pdfDoc.add(cmykImage);

        document.close();

        File outputFile = new File("target/pdfx1a-output.pdf");
        try (OutputStream out = new FileOutputStream(outputFile)) {
            baos.writeTo(out);
        }


        String pdfText = baos.toString("ISO-8859-1");
        assertThat(pdfText).contains("%PDF-1.3");
        assertThat(pdfText).contains("/GTS_PDFX");
        assertThat(pdfText).contains("/OutputConditionIdentifier");
        assertThat(pdfText).contains("/FontDescriptor");
        assertThat(pdfText).contains("/FontFile")
                .withFailMessage("Embedded font is missing");
    }

    private byte[] getLiberationFontByte() throws IOException {
        try (InputStream stream = BaseFont.getResourceStream("fonts/liberation/LiberationSerif-Regular.ttf", null)) {
            assertThat(stream).isNotNull();
            return IOUtils.toByteArray(stream);
        }
    }

    private byte[] getCmykImageBytes() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/caesar_coin_cmyk.jpg")) {
            assertThat(stream).isNotNull(); // Ensure the image is found
            return IOUtils.toByteArray(stream);
        }
    }
}
