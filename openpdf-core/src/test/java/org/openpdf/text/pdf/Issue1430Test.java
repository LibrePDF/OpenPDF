package org.openpdf.text.pdf;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test that demonstrates the fix for the exact use case described in issue #1430.
 * 
 * This test shows how the new setFirstLineBaselineY() API solves the problem
 * where users want to set the first line baseline without having to calculate
 * yLine backwards from the desired baseline and leading.
 */
public class Issue1430Test {

    /**
     * This test demonstrates the exact problem described in issue #1430
     * and shows how the new API solves it.
     */
    @Test
    public void testIssue1430OriginalProblem() throws Exception {
        File outputPDF = new File("target/issue-1430-solution.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Issue #1430 Solution Test\n\n"));

        PdfContentByte cb = writer.getDirectContent();
        ColumnText ct = new ColumnText(cb);

        // Setup from the issue
        float llx = 50f, lly = 500f, urx = 300f, ury = 700f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        // Create font
        Font font = new Font(Font.HELVETICA, 12);
        
        // The issue description states:
        // "We want firstYLine == baseline but ColumnText writes firstYLine = yLine - leading"
        // With the old approach, if we want baseline at ury - dAscent (694.68),
        // we would have to set yLine = 694.68 + 14 = 708.68
        // But with the new API, we can set the baseline directly!
        
        // Calculate desired baseline (from issue: ury - dAscent where dAscent = 5.32)
        float dAscent = 5.32f;
        float desiredBaseline = ury - dAscent; // This should be 694.68
        
        // NEW API: Set first line baseline directly!
        ct.setFirstLineBaselineY(desiredBaseline);
        
        // Create the text (from the issue)
        Chunk chunk = new Chunk("Hello, world\nWelcome to OpenPDF", font);
        chunk.setCharacterSpacing(0.4f);
        chunk.setHorizontalScaling(1f);
        
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setLeading(14f);
        ct.addText(paragraph);
        
        // Render
        int status = ct.go();
        
        // Verify success
        assertTrue((status & ColumnText.NO_MORE_TEXT) != 0 || (status & ColumnText.NO_MORE_COLUMN) == 0,
                "Text should be rendered successfully");

        document.close();

        // Write to file for manual inspection
        try (FileOutputStream fos = new FileOutputStream(outputPDF)) {
            fos.write(baos.toByteArray());
        }
        
        System.out.println("Issue #1430 solution PDF created: " + outputPDF.getAbsolutePath());
    }

    /**
     * Test comparing old behavior (manual calculation) vs new API
     */
    @Test
    public void testOldVsNewBehavior() throws Exception {
        File outputPDF = new File("target/issue-1430-comparison.pdf");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Comparison: Old Approach vs New API\n\n"));

        PdfContentByte cb = writer.getDirectContent();
        Font font = new Font(Font.HELVETICA, 12);
        float leading = 14f;

        // Left column: Old approach (requires manual calculation)
        ColumnText ctOld = new ColumnText(cb);
        float llx1 = 50f, lly1 = 600f, urx1 = 250f, ury1 = 750f;
        ctOld.setSimpleColumn(llx1, lly1, urx1, ury1);
        
        float desiredBaseline1 = 740f;
        // Old way: must calculate yLine = baseline + leading
        ctOld.setYLine(desiredBaseline1 + leading);
        
        Paragraph para1 = new Paragraph("OLD APPROACH:\nManual calculation\nyLine = baseline + leading", font);
        para1.setLeading(leading);
        ctOld.addText(para1);
        ctOld.go();

        // Right column: New API (simple and direct)
        ColumnText ctNew = new ColumnText(cb);
        float llx2 = 300f, lly2 = 600f, urx2 = 500f, ury2 = 750f;
        ctNew.setSimpleColumn(llx2, lly2, urx2, ury2);
        
        float desiredBaseline2 = 740f;
        // New way: simply set the baseline directly!
        ctNew.setFirstLineBaselineY(desiredBaseline2);
        
        Paragraph para2 = new Paragraph("NEW API:\nDirect baseline control\nNo calculation needed!", font);
        para2.setLeading(leading);
        ctNew.addText(para2);
        ctNew.go();

        document.close();

        // Write to file
        try (FileOutputStream fos = new FileOutputStream(outputPDF)) {
            fos.write(baos.toByteArray());
        }
        
        System.out.println("Comparison PDF created: " + outputPDF.getAbsolutePath());
    }
}
