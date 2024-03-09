/*
 * $Id: FontFactoryStyles.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.fonts.getting;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Changing the style of a FontFactory Font.
 */
public class FontFactoryStyles {

    /**
     * Changing the style of a FontFactory Font.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Changing the style of a FontFactory font");

        // step 1: creation of a document-object
        Document document = new Document();

        try {

            // step 2: creation of the writer
            PdfWriter.getInstance(document, new FileOutputStream("fontfactorystyles.pdf"));

            // step 3: we open the document
            document.open();

            // step 4: we add some content
            FontFactory.register("c:\\windows\\fonts\\arial.ttf");
            FontFactory.register("c:\\windows\\fonts\\arialbd.ttf");
            FontFactory.register("c:\\windows\\fonts\\ariali.ttf");
            FontFactory.register("c:\\windows\\fonts\\arialbi.ttf");
            Phrase myPhrase = new Phrase("This is font family Arial ", FontFactory.getFont("Arial", 8));
            myPhrase.add(new Phrase("italic ", FontFactory.getFont("Arial", 8, Font.ITALIC)));
            myPhrase.add(new Phrase("bold ", FontFactory.getFont("Arial", 8, Font.BOLD)));
            myPhrase.add(new Phrase("bolditalic", FontFactory.getFont("Arial", 8, Font.BOLDITALIC)));
            document.add(myPhrase);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }
}
