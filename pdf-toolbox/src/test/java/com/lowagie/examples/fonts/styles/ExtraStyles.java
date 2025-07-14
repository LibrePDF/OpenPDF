/*
 * $Id: ExtraStyles.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.fonts.styles;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates how to underline and strike through text.
 *
 * @author blowagie
 */

class ExtraStyles {

    /**
     * Underline or strike through text.
     */
    @Test
    void testUnderlineAndStrikeThrough() throws Exception {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // step 1: creation of a document-object
            Document document = new Document();
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document, baos);

            // step 3: we open the document
            document.open();
            // step 4:
            Font font;
            Chunk chunk;
            font = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.UNDERLINE);
            chunk = new Chunk("underline", font);
            document.add(chunk);
            font = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.NORMAL);
            chunk = new Chunk(" and ", font);
            document.add(chunk);
            font = FontFactory.getFont(FontFactory.HELVETICA, Font.DEFAULTSIZE, Font.STRIKETHRU);
            chunk = new Chunk("strike through", font);
            document.add(chunk);

            // step 5: we close the document
            document.close();

            assertNotEquals(0, baos.size());
        }

    }
}
