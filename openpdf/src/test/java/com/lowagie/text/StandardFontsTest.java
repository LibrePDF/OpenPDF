package com.lowagie.text;

import com.lowagie.text.pdf.PdfWriter;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lowagie.text.StandardFonts.COURIER;
import static com.lowagie.text.StandardFonts.COURIER_BOLD;
import static com.lowagie.text.StandardFonts.COURIER_BOLDITALIC;
import static com.lowagie.text.StandardFonts.COURIER_ITALIC;
import static com.lowagie.text.StandardFonts.HELVETICA;
import static com.lowagie.text.StandardFonts.HELVETICA_BOLD;
import static com.lowagie.text.StandardFonts.HELVETICA_BOLDITALIC;
import static com.lowagie.text.StandardFonts.HELVETICA_ITALIC;
import static com.lowagie.text.StandardFonts.SYMBOL;
import static com.lowagie.text.StandardFonts.TIMES;
import static com.lowagie.text.StandardFonts.TIMES_BOLD;
import static com.lowagie.text.StandardFonts.TIMES_BOLDITALIC;
import static com.lowagie.text.StandardFonts.TIMES_ITALIC;
import static com.lowagie.text.StandardFonts.ZAPFDINGBATS;
import static com.lowagie.text.StandardFonts.values;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            final List<StandardFonts> standardFonts = Arrays.stream(values())
                    .filter(f -> !f.isDeprecated()).collect(Collectors.toList());
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
        final List<StandardFonts> standardFonts = Arrays.stream(values())
                .filter(f -> !f.isDeprecated()).collect(Collectors.toList());
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
        final List<StandardFonts> standardFonts = Arrays.stream(values())
                .filter(f -> !f.isDeprecated()).collect(Collectors.toList());
        for (StandardFonts standardFont : standardFonts) {
            // when
            final Font font = standardFont.create();
            // then
            assertNotNull(font);
        }
    }

    @Test
    void testCreateStandardDeprecatedFonts() {
        // given
        SoftAssertions softly = new SoftAssertions();
        final List<StandardFonts> deprecatedFonts = Arrays.stream(values())
                .filter(StandardFonts::isDeprecated).collect(Collectors.toList());
        // when
        for (StandardFonts deprecatedFont : deprecatedFonts) {
            // then
            softly.assertThatThrownBy(deprecatedFont::create)
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining(deprecatedFont.name());
        }
        softly.assertAll();
    }
}