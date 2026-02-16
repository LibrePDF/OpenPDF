package org.openpdf.text.pdf;

import org.openpdf.text.*;
import org.openpdf.text.pdf.BaseFont;

import java.io.FileOutputStream;

/**
 * Example demonstrating the new setFirstLineBaselineY() API.
 * 
 * This class shows how to use the new API to precisely control the baseline Y
 * position of the first line in a ColumnText, addressing the issue described in
 * GitHub issue #1430.
 */
public class FirstLineBaselineExample {

    public static void main(String[] args) {
        try {
            // Create document
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, 
                new FileOutputStream("target/first-line-baseline-example.pdf"));
            document.open();

            // Add a title
            document.add(new Paragraph("First Line Baseline Control Example\n\n"));

            PdfContentByte cb = writer.getDirectContent();

            // Example 1: Without custom baseline (old behavior)
            demonstrateOldBehavior(cb, document);

            // Example 2: With custom baseline (new API)
            demonstrateNewAPI(cb);

            document.close();
            System.out.println("PDF created successfully: target/first-line-baseline-example.pdf");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Demonstrates the old behavior where you need to calculate yLine
     * to achieve a desired first-line baseline.
     */
    private static void demonstrateOldBehavior(PdfContentByte cb, Document document) 
            throws Exception {
        document.add(new Paragraph("Example 1: Old Behavior (Manual Calculation)\n"));
        
        ColumnText ct = new ColumnText(cb);
        float llx = 50f, lly = 400f, urx = 300f, ury = 550f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        Font font = new Font(Font.HELVETICA, 12);
        float leading = 14f;
        
        // With old behavior, to set first line baseline at 540, you need to:
        // 1. Calculate yLine = desiredBaseline + leading
        // 2. Account for useAscender flag
        // 3. Account for effective leading calculation
        float desiredBaseline = 540f;
        ct.setYLine(desiredBaseline + leading);  // Manual calculation required!

        Paragraph paragraph = new Paragraph("This text uses the old approach.\n" +
                "First line baseline had to be calculated manually.", font);
        paragraph.setLeading(leading);
        ct.addText(paragraph);
        ct.go();

        document.add(new Paragraph("\n"));
    }

    /**
     * Demonstrates the new API for setting first-line baseline directly.
     */
    private static void demonstrateNewAPI(PdfContentByte cb) throws Exception {
        ColumnText ct = new ColumnText(cb);
        float llx = 50f, lly = 200f, urx = 300f, ury = 350f;
        ct.setSimpleColumn(llx, lly, urx, ury);

        Font font = new Font(Font.HELVETICA, 12);
        float leading = 14f;

        // With new API, simply set the desired baseline Y directly!
        float desiredBaseline = 340f;
        ct.setFirstLineBaselineY(desiredBaseline);  // Simple and intuitive!

        Paragraph paragraph = new Paragraph("This text uses the new API.\n" +
                "First line baseline is set directly to " + desiredBaseline + ".", font);
        paragraph.setLeading(leading);
        ct.addText(paragraph);
        ct.go();
    }

    /**
     * Advanced example showing multiple blocks with precise positioning.
     */
    public static void createAdvancedExample() throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, 
            new FileOutputStream("target/first-line-baseline-advanced.pdf"));
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        Font font = new Font(Font.HELVETICA, 10);

        // Create a grid-like layout with precise baseline positioning
        float[] baselineYPositions = {800f, 750f, 700f, 650f, 600f};
        
        for (int i = 0; i < baselineYPositions.length; i++) {
            ColumnText ct = new ColumnText(cb);
            ct.setSimpleColumn(50f, 500f, 300f, 820f);
            
            // Set first line baseline to exact position
            ct.setFirstLineBaselineY(baselineYPositions[i]);
            
            Paragraph para = new Paragraph("Line " + (i + 1) + 
                    " at baseline Y = " + baselineYPositions[i], font);
            ct.addText(para);
            ct.go();
        }

        document.close();
        System.out.println("Advanced PDF created: target/first-line-baseline-advanced.pdf");
    }
}
