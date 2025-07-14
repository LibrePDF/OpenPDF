package org.librepdf.openpdf.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class LiberationTest {

    @Test
    void createDocumentAllFonts() {
        try (// step 1: we create a writer that listens to the document
                FileOutputStream outputStream = new FileOutputStream("target/LiberationFonts.pdf");
                // step 2: creation of a document-object
                Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            // step 3: we open the document
            document.open();
            /* step 4:*/
            // the 14 standard fonts in PDF: do not use this Font constructor!
            // this is for demonstration purposes only, use FontFactory!
            for (Liberation liberationFont : Liberation.values()) {
                // add the content
                Font font = liberationFont.create();
                document.add(new Paragraph(
                        "quick brown fox jumps over the lazy dog. <= " + liberationFont, font));
            }
        } catch (DocumentException | IOException de) {
            de.printStackTrace();
        }
    }

    @Test
    void createDocumentAllFontsUnicode() {
        try (FileOutputStream outputStream = new FileOutputStream(
                "target/LiberationFontsUnicode.pdf");
                Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            for (Liberation liberationFont : Liberation.values()) {
                Font font = liberationFont.create();
                document.add(new Paragraph(" => äöüÄÖÜß€µł¶ŧ←↓→øþæſðđŋħ»«¢„“”µ·…–\u25b2 <= "
                        + liberationFont, font));
            }
        } catch (DocumentException | IOException de) {
            de.printStackTrace();
        }
    }

}
