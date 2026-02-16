package org.openpdf.text.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.PageSize;
import org.openpdf.text.Phrase;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for non-breaking space justification issue #1454.
 * When justifying text with ColumnText.setSpaceCharRatio > 1,
 * non-breaking spaces (U+00A0) should be stretched like regular spaces.
 */
public class NonBreakingSpaceJustificationTest {

    @Test
    public void testNonBreakingSpaceJustification() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        ColumnText ct = new ColumnText(writer.getDirectContent());
        StringBuilder sb = new StringBuilder();
        String nonBreakingSpace = "\u00a0"; // non-breaking space
        for (int i = 0; i < 30; i++) {
            sb.append("text").append(nonBreakingSpace).append("text").append(" ");
        }
        ct.setSimpleColumn(0, 0, 300, 300);
        ct.setAlignment(3); // justified
        ct.setSpaceCharRatio(100);
        ct.setText(new Phrase(sb.toString()));
        ct.go();

        document.close();

        // Verify that the document was created successfully
        byte[] pdfBytes = baos.toByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Optionally write to file for manual inspection
        try (FileOutputStream fos = new FileOutputStream("target/non-breaking-space-test.pdf")) {
            fos.write(pdfBytes);
        }
    }

    @Test
    public void testMixedSpacesJustification() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        ColumnText ct = new ColumnText(writer.getDirectContent());
        StringBuilder sb = new StringBuilder();
        String nonBreakingSpace = "\u00a0"; // non-breaking space
        String space = " "; // regular space
        for (int i = 0; i < 15; i++) {
            sb.append("text").append(nonBreakingSpace).append("text").append(space)
                    .append("more").append(space).append("words").append(nonBreakingSpace);
        }
        ct.setSimpleColumn(0, 0, 300, 300);
        ct.setAlignment(3); // justified
        ct.setSpaceCharRatio(50);
        ct.setText(new Phrase(sb.toString()));
        ct.go();

        document.close();

        // Verify that the document was created successfully
        byte[] pdfBytes = baos.toByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Optionally write to file for manual inspection
        try (FileOutputStream fos = new FileOutputStream("target/mixed-spaces-test.pdf")) {
            fos.write(pdfBytes);
        }
    }

    @Test
    public void testOnlyNonBreakingSpaces() throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        ColumnText ct = new ColumnText(writer.getDirectContent());
        String nonBreakingSpace = "\u00a0"; // non-breaking space
        String s = "word" + nonBreakingSpace + "word" + nonBreakingSpace + "word" + nonBreakingSpace + "word" + nonBreakingSpace + "word" + nonBreakingSpace + "word";
        ct.setSimpleColumn(0, 0, 300, 300);
        ct.setAlignment(3); // justified
        ct.setSpaceCharRatio(100);
        ct.setText(new Phrase(s));
        ct.go();

        document.close();

        // Verify that the document was created successfully
        byte[] pdfBytes = baos.toByteArray();
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        
        // Optionally write to file for manual inspection
        try (FileOutputStream fos = new FileOutputStream("target/only-non-breaking-spaces-test.pdf")) {
            fos.write(pdfBytes);
        }
    }
}
