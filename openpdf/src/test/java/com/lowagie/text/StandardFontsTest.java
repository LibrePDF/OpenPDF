package com.lowagie.text;

import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;

class StandardFontsTest {

    @Test
    void createDocumentAllFonts() {
        try (// step 1: we create a writer that listens to the document
             FileOutputStream outputStream = new FileOutputStream("target/StandardFonts.pdf");
             // step 2: creation of a document-object
             Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            // step 3: we open the document
            document.open();
            /* step 4:*/
            // the 14 standard fonts in PDF: do not use this Font constructor!
            // this is for demonstration purposes only, use FontFactory!
            for (StandardFonts standardFont : StandardFonts.values()) {
                // add the content
                Font font = standardFont.create();
                document.add(new Paragraph("quick brown fox jumps over the lazy dog. <= " + standardFont, font));
            }
        } catch (DocumentException | IOException de) {
            de.printStackTrace();
        }
    }

    @Test
    void createDocumentAllFontsUnicode() {
        try (FileOutputStream outputStream = new FileOutputStream("target/StandardFontsUnicode.pdf");
             Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            for (StandardFonts standardFont : StandardFonts.values()) {
                Font font = standardFont.create();
                document.add(new Paragraph(" => äöüÄÖÜß€µł¶ŧ←↓→øþæſðđŋħ»«¢„“”µ·…–\u25b2 <= "
                        + standardFont, font));
            }
        } catch (DocumentException | IOException de) {
            de.printStackTrace();
        }
    }
}