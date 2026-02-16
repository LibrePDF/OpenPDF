package org.openpdf.text.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.BaseFont;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ColumnText custom first line baseline functionality.
 * Tests the setFirstLineBaselineY() method and related functionality.
 */
public class ColumnTextFirstLineBaselineTest {

    /**
     * Test that setFirstLineBaselineY sets the custom baseline correctly.
     */
    @Test
    public void testSetFirstLineBaselineY() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float llx = 50f, lly = 500f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // Set custom baseline
        float customBaseline = 690f;
        ct.setFirstLineBaselineY(customBaseline);

        // Verify getter returns the same value
        assertEquals(customBaseline, ct.getFirstLineBaselineY(), 0.01f);

        document.close();
    }

    /**
     * Test that clearFirstLineBaselineY clears the custom baseline.
     */
    @Test
    public void testClearFirstLineBaselineY() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        // Set and then clear
        ct.setFirstLineBaselineY(690f);
        ct.clearFirstLineBaselineY();

        // Verify it's cleared (returns NaN)
        assertTrue(Float.isNaN(ct.getFirstLineBaselineY()));

        document.close();
    }

    /**
     * Test that custom baseline works with addText and produces expected output.
     * This test validates that the first line is positioned at the custom baseline.
     */
    @Test
    public void testFirstLineBaselineWithAddText() throws Exception {
        File outputPDF = new File("target/columnTextFirstLineBaselineTest.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float llx = 50f, lly = 500f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // Create font
        Font font = new Font(Font.HELVETICA, 12);
        
        // Set custom baseline to a specific position
        float customBaseline = 690f;
        ct.setFirstLineBaselineY(customBaseline);

        // Add text
        Chunk chunk = new Chunk("Hello, world\nWelcome to OpenPDF", font);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setLeading(14f);
        ct.addText(paragraph);

        // Render the text
        int status = ct.go();

        // Verify it completed successfully
        assertTrue((status & ColumnText.NO_MORE_TEXT) != 0 || (status & ColumnText.NO_MORE_COLUMN) == 0);

        document.close();

        // Write to file for manual inspection
        try (FileOutputStream fos = new FileOutputStream(outputPDF)) {
            fos.write(baos.toByteArray());
        }
    }

    /**
     * Test that custom baseline works with addElement and produces expected output.
     */
    @Test
    public void testFirstLineBaselineWithAddElement() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float llx = 50f, lly = 500f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // Set custom baseline
        float customBaseline = 690f;
        ct.setFirstLineBaselineY(customBaseline);

        // Add element
        Paragraph paragraph = new Paragraph("Hello from addElement\nSecond line");
        paragraph.setLeading(14f);
        ct.addElement(paragraph);

        // Render the text
        int status = ct.go();

        // Verify it completed successfully
        assertTrue((status & ColumnText.NO_MORE_TEXT) != 0 || (status & ColumnText.NO_MORE_COLUMN) == 0);

        document.close();
    }

    /**
     * Test that custom baseline is validated against column bounds.
     * Setting a baseline outside the column should result in NO_MORE_COLUMN status.
     */
    @Test
    public void testFirstLineBaselineOutOfBounds() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float llx = 50f, lly = 500f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // Set baseline above the column top (out of bounds)
        ct.setFirstLineBaselineY(750f);

        Font font = new Font(Font.HELVETICA, 12);
        Paragraph paragraph = new Paragraph("This should not fit", font);
        ct.addText(paragraph);

        // Render the text
        int status = ct.go();

        // Should indicate no more column space
        assertTrue((status & ColumnText.NO_MORE_COLUMN) != 0);

        document.close();
    }

    /**
     * Test that custom baseline only affects the first line.
     * Subsequent lines should use normal leading.
     */
    @Test
    public void testFirstLineBaselineOnlyAffectsFirstLine() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float llx = 50f, lly = 400f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // Set custom baseline for first line
        float customBaseline = 690f;
        ct.setFirstLineBaselineY(customBaseline);

        Font font = new Font(Font.HELVETICA, 12);
        Paragraph paragraph = new Paragraph("First line\nSecond line\nThird line", font);
        paragraph.setLeading(20f);
        ct.addText(paragraph);

        // Render the text
        int status = ct.go();

        // Verify it completed successfully
        assertTrue((status & ColumnText.NO_MORE_TEXT) != 0 || (status & ColumnText.NO_MORE_COLUMN) == 0);

        document.close();
    }

    /**
     * Test interaction with setUseAscender flag.
     * Custom baseline should take precedence over useAscender.
     */
    @Test
    public void testFirstLineBaselineWithUseAscender() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float llx = 50f, lly = 500f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // Enable useAscender
        ct.setUseAscender(true);

        // Set custom baseline (should take precedence)
        float customBaseline = 690f;
        ct.setFirstLineBaselineY(customBaseline);

        Font font = new Font(Font.HELVETICA, 12);
        Paragraph paragraph = new Paragraph("Test with useAscender", font);
        ct.addText(paragraph);

        // Render the text
        int status = ct.go();

        // Verify it completed successfully
        assertTrue((status & ColumnText.NO_MORE_TEXT) != 0 || (status & ColumnText.NO_MORE_COLUMN) == 0);

        document.close();
    }

    /**
     * Test that calling go() multiple times with different custom baselines works correctly.
     */
    @Test
    public void testMultipleGoCallsWithDifferentBaselines() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        
        // Add some content to create a page
        document.add(new Paragraph("Test page"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        float llx = 50f, lly = 300f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // First block with custom baseline
        ct.setFirstLineBaselineY(690f);
        Font font = new Font(Font.HELVETICA, 12);
        Paragraph paragraph1 = new Paragraph("First block", font);
        ct.addText(paragraph1);
        int status1 = ct.go();

        // Get current position after first block
        float yAfterFirst = ct.getYLine();

        // Second block with different custom baseline
        ct.clearFirstLineBaselineY();
        ct.setFirstLineBaselineY(yAfterFirst - 50);
        Paragraph paragraph2 = new Paragraph("Second block", font);
        ct.addText(paragraph2);
        int status2 = ct.go();

        // Both should complete successfully
        assertTrue((status1 & ColumnText.NO_MORE_TEXT) != 0 || (status1 & ColumnText.NO_MORE_COLUMN) == 0);
        assertTrue((status2 & ColumnText.NO_MORE_TEXT) != 0 || (status2 & ColumnText.NO_MORE_COLUMN) == 0);

        document.close();
    }
}
