package org.openpdf.text;

import static org.openpdf.text.StandardFonts.COURIER;
import static org.openpdf.text.StandardFonts.COURIER_BOLD;
import static org.openpdf.text.StandardFonts.COURIER_BOLDITALIC;
import static org.openpdf.text.StandardFonts.COURIER_ITALIC;
import static org.openpdf.text.StandardFonts.HELVETICA;
import static org.openpdf.text.StandardFonts.HELVETICA_BOLD;
import static org.openpdf.text.StandardFonts.HELVETICA_BOLDITALIC;
import static org.openpdf.text.StandardFonts.HELVETICA_ITALIC;
import static org.openpdf.text.StandardFonts.SYMBOL;
import static org.openpdf.text.StandardFonts.TIMES;
import static org.openpdf.text.StandardFonts.TIMES_BOLD;
import static org.openpdf.text.StandardFonts.TIMES_BOLDITALIC;
import static org.openpdf.text.StandardFonts.TIMES_ITALIC;
import static org.openpdf.text.StandardFonts.ZAPFDINGBATS;
import static org.openpdf.text.StandardFonts.values;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.openpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

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
            final List<StandardFonts> standardFonts = List.of(values());
            for (StandardFonts standardFont : standardFonts) {
                // add the content
                Font font = standardFont.create();
                document.add(new Paragraph(
                        "quick brown fox jumps over the lazy dog. <= " + standardFont, font));
            }
        } catch (DocumentException | IOException de) {
            de.printStackTrace();
        }
    }

    @Test
    void testNonDeprecatedFonts() {
        // given
        final List<StandardFonts> standardFonts = List.of(values());
        // then
        assertThat(standardFonts).containsExactlyInAnyOrder(
                COURIER, COURIER_BOLD, COURIER_BOLDITALIC, COURIER_ITALIC,
                HELVETICA, HELVETICA_BOLD, HELVETICA_BOLDITALIC, HELVETICA_ITALIC,
                SYMBOL,
                TIMES, TIMES_BOLD, TIMES_BOLDITALIC, TIMES_ITALIC,
                ZAPFDINGBATS
        );
    }

    @Test
    void testCreateStandardFonts() throws IOException {
        // given
        final List<StandardFonts> standardFonts = List.of(values());
        for (StandardFonts standardFont : standardFonts) {
            // when
            final Font font = standardFont.create();
            // then
            assertNotNull(font);
        }
    }
}
