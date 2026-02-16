/*
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 */
package org.openpdf.examples.objects.images;

import java.io.FileOutputStream;
import java.io.IOException;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Image;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Demonstrates how to place images inline with text, including seals and stamps.
 * This example addresses the common use case of placing an official seal on a signature line
 * without the image occupying its own line.
 */
public class InlineImageWithText {

    /**
     * Creates a PDF demonstrating inline image placement with text wrapping and underlying images.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Inline images with text - seals, stamps, and text wrapping");
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("inlineImageWithText.pdf"));
            document.open();

            // Create fonts for demonstration
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Example 1: Official seal placed inline with signature text (TEXTWRAP)
            document.add(new Paragraph("Example 1: Seal inline with signature using TEXTWRAP", boldFont));
            document.add(Chunk.NEWLINE);

            // Load the seal image (using a sample image as a seal)
            Image sealImage = Image.getInstance("hitchcock.png");
            sealImage.scaleToFit(80f, 80f); // Scale to appropriate size
            // Use RIGHT | TEXTWRAP to place seal on the right with text wrapping on the left
            sealImage.setAlignment(Image.RIGHT | Image.TEXTWRAP);

            // Add seal image
            document.add(sealImage);

            // Add signature text that will wrap around the seal
            Paragraph signature = new Paragraph();
            signature.setFont(normalFont);
            signature.add("Signature: John Smith\n");
            signature.add("Date: January 1, 2025\n");
            signature.add("Title: Director\n");
            signature.add("This contract is hereby approved and sealed with the official company seal.\n");
            document.add(signature);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Example 2: Watermark/stamp behind text (UNDERLYING)
            document.add(new Paragraph("Example 2: Stamp placed behind text using UNDERLYING", boldFont));
            document.add(Chunk.NEWLINE);

            Image stampImage = Image.getInstance("hitchcock.png");
            stampImage.scaleToFit(100f, 100f);
            // Use RIGHT | UNDERLYING to place stamp behind text on the right
            stampImage.setAlignment(Image.RIGHT | Image.UNDERLYING);

            document.add(stampImage);

            Paragraph content = new Paragraph();
            content.setFont(normalFont);
            content.add("This is important content that needs to be stamped. ");
            content.add("The stamp will appear behind this text, acting as a watermark or official seal. ");
            content.add("This technique is particularly useful for contracts, certificates, and official documents ");
            content.add("where you want the seal to be visible but not disrupt the text flow.\n");
            document.add(content);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Example 3: Image on left with text wrapping on the right
            document.add(new Paragraph("Example 3: Image on left with text wrapping using LEFT | TEXTWRAP", boldFont));
            document.add(Chunk.NEWLINE);

            Image leftImage = Image.getInstance("vonnegut.gif");
            leftImage.scaleToFit(100f, 100f);
            // Use LEFT | TEXTWRAP to place image on left with text wrapping on the right
            leftImage.setAlignment(Image.LEFT | Image.TEXTWRAP);

            document.add(leftImage);

            for (int i = 0; i < 50; i++) {
                document.add(new Phrase("This text wraps around the image on the left. ", normalFont));
            }

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Example 4: Multiple signatures with seals
            document.add(new Paragraph("Example 4: Contract with multiple signatures and seals", boldFont));
            document.add(Chunk.NEWLINE);

            document.add(new Paragraph("This contract is hereby signed by:", normalFont));
            document.add(Chunk.NEWLINE);

            // First signature with seal
            Image seal1 = Image.getInstance("hitchcock.png");
            seal1.scaleToFit(60f, 60f);
            seal1.setAlignment(Image.RIGHT | Image.TEXTWRAP);
            document.add(seal1);

            Paragraph sig1 = new Paragraph();
            sig1.setFont(normalFont);
            sig1.add("Signature 1: Jane Doe\n");
            sig1.add("Position: CEO\n");
            sig1.add("Date: January 1, 2025\n");
            document.add(sig1);

            document.add(Chunk.NEWLINE);

            // Second signature with seal (position varies based on content)
            Image seal2 = Image.getInstance("hitchcock.png");
            seal2.scaleToFit(60f, 60f);
            seal2.setAlignment(Image.RIGHT | Image.TEXTWRAP);
            document.add(seal2);

            Paragraph sig2 = new Paragraph();
            sig2.setFont(normalFont);
            sig2.add("Signature 2: Robert Johnson\n");
            sig2.add("Position: CFO\n");
            sig2.add("Date: January 1, 2025\n");
            document.add(sig2);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Summary paragraph
            Paragraph summary = new Paragraph();
            summary.setFont(normalFont);
            summary.add("Summary of alignment flags:\n\n");
            summary.add("• Image.TEXTWRAP - Allows text to flow around the image (useful for inline images)\n");
            summary.add("• Image.UNDERLYING - Places the image behind text (useful for watermarks/stamps)\n");
            summary.add("• Image.LEFT | Image.TEXTWRAP - Image on left, text wraps on right\n");
            summary.add("• Image.RIGHT | Image.TEXTWRAP - Image on right, text wraps on left\n");
            summary.add("• Image.LEFT | Image.UNDERLYING - Image behind text on left\n");
            summary.add("• Image.RIGHT | Image.UNDERLYING - Image behind text on right\n");
            summary.add("\nThese flags can be combined using the bitwise OR operator (|).\n");
            document.add(summary);

        } catch (DocumentException | IOException e) {
            System.err.println(e.getMessage());
        }
        document.close();
        System.out.println("PDF created successfully: inlineImageWithText.pdf");
    }
}
