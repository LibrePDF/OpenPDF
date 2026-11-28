package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.openpdf.text.Anchor;
import org.openpdf.text.Document;
import org.openpdf.text.HeaderFooter;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;

class PdfDocumentFooterAnchorTest {

    private static final String URL = "https://github.com/LibrePDF/OpenPDF";

    private static HeaderFooter footerWithLink() {
        Anchor anchor = new Anchor("OpenPDF");
        anchor.setReference(URL);
        Phrase phrase = new Phrase("Made with ");
        phrase.add(anchor);
        return new HeaderFooter(phrase, false);
    }

    private static int linkCount(PdfReader reader, int page) {
        PdfArray annotations = reader.getPageN(page).getAsArray(PdfName.ANNOTS);
        int links = 0;
        if (annotations != null) {
            for (int i = 0; i < annotations.size(); i++) {
                PdfDictionary annotation = annotations.getAsDict(i);
                PdfDictionary action = annotation.getAsDict(PdfName.A);
                if (PdfName.LINK.equals(annotation.getAsName(PdfName.SUBTYPE))
                        && action != null && URL.equals(action.getAsString(PdfName.URI).toString())) {
                    links++;
                }
            }
        }
        return links;
    }

    @Test
    void documentWithLinkInFooterCloses() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.setFooter(footerWithLink());
        document.open();
        document.add(new Paragraph("First page"));
        document.newPage();
        document.add(new Paragraph("Second page"));
        document.close();

        try (PdfReader reader = new PdfReader(out.toByteArray())) {
            assertThat(reader.getNumberOfPages()).isEqualTo(2);
            assertThat(linkCount(reader, 1)).isEqualTo(1);
            assertThat(linkCount(reader, 2)).isEqualTo(1);
        }
    }

    @Test
    void annotationForPageThatNeverComesStillFails() throws Exception {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, new ByteArrayOutputStream());
        document.open();
        document.add(new Paragraph("Only page"));
        PdfAnnotation annotation = PdfAnnotation.createText(writer, new Rectangle(100, 100, 120, 120),
                "Note", "For a page that is never added", false, null);
        annotation.setPlaceInPage(3);
        writer.addAnnotation(annotation);

        assertThatThrownBy(document::close).hasMessageContaining("annotations");
    }

    @Test
    void annotationForLaterPageIsPlacedThere() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("First page"));
        PdfAnnotation annotation = PdfAnnotation.createText(writer, new Rectangle(100, 100, 120, 120),
                "Note", "For the second page", false, null);
        annotation.setPlaceInPage(2);
        writer.addAnnotation(annotation);
        document.newPage();
        document.add(new Paragraph("Second page"));
        document.close();

        try (PdfReader reader = new PdfReader(out.toByteArray())) {
            assertThat(reader.getPageN(1).getAsArray(PdfName.ANNOTS)).isNull();
            assertThat(reader.getPageN(2).getAsArray(PdfName.ANNOTS).size()).isEqualTo(1);
        }
    }
}
