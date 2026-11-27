package org.openpdf.text.pdf;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.PageSize;

/**
 * Fields, outlines, name trees and number trees whose links lead back to a node that was already visited must not
 * make the code that walks them loop or recurse without end.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class PdfStructureCycleTest {

    private static final String PAGES = "<< /Type /Pages /Kids [3 0 R] /Count 1 >>";
    private static final String PAGE = "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] >>";
    private static final String PAGE_WITH_WIDGET =
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 10 10] /Annots [4 0 R] >>";
    private static final String WIDGET =
            "<< /Type /Annot /Subtype /Widget /FT /Tx /T (w) /Rect [0 0 1 1] /Parent 5 0 R >>";

    @Test
    void acroFieldsStopAtParentCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /AcroForm << /Fields [5 0 R] >> >>",
                PAGES, PAGE_WITH_WIDGET, WIDGET,
                "<< /T (a) /Kids [4 0 R] /Parent 6 0 R >>",
                "<< /T (b) /Parent 5 0 R >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getAcroFields().getAllFields().keySet()).containsExactly("b.a.w");
        }
    }

    @Test
    void acroFieldsStillReadRegularHierarchy() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /AcroForm << /Fields [6 0 R] >> >>",
                PAGES, PAGE_WITH_WIDGET, WIDGET,
                "<< /T (a) /Kids [4 0 R] /Parent 6 0 R >>",
                "<< /T (b) /Kids [5 0 R] >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(reader.getAcroFields().getAllFields().keySet()).containsExactly("b.a.w");
            assertThat(reader.getAcroForm().getFields()).hasSize(1);
            assertThat(reader.getAcroForm().getField("b.a.w")).isNotNull();
        }
    }

    @Test
    void acroFormStopsAtKidsCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /AcroForm << /Fields [4 0 R] >> >>",
                PAGES, PAGE,
                "<< /T (a) /Kids [5 0 R 6 0 R] >>",
                "<< /T (b) /Kids [4 0 R] >>",
                "<< /T (c) /FT /Tx >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            PRAcroForm form = reader.getAcroForm();

            assertThat(form.getFields()).hasSize(1);
            assertThat(form.getField("a.c")).isNotNull();
        }
    }

    @Test
    void fdfReaderStopsAtKidsCycle() throws Exception {
        String fdf = "%FDF-1.2\n"
                + "1 0 obj\n<< /FDF << /Fields [2 0 R 3 0 R] >> >>\nendobj\n"
                + "2 0 obj\n<< /T (a) /Kids [2 0 R] >>\nendobj\n"
                + "3 0 obj\n<< /T (b) /V (value) >>\nendobj\n"
                + "trailer\n<< /Root 1 0 R >>\n%%EOF\n";

        try (FdfReader reader = new FdfReader(fdf.getBytes(ISO_8859_1))) {
            assertThat(reader.getAllFields().keySet()).containsExactly("b");
            assertThat(reader.getFieldValue("b")).isEqualTo("value");
        }
    }

    @Test
    void bookmarksStopAtNextCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /Outlines 4 0 R >>", PAGES, PAGE,
                "<< /Type /Outlines /First 5 0 R /Last 6 0 R /Count 2 >>",
                "<< /Title (A) /Parent 4 0 R /Next 6 0 R >>",
                "<< /Title (B) /Parent 4 0 R /Next 5 0 R >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            List<Map<String, Object>> bookmarks = SimpleBookmark.getBookmarkList(reader);

            assertThat(bookmarks).extracting(bookmark -> bookmark.get("Title")).containsExactly("A", "B");
        }
    }

    @Test
    void bookmarksStopAtFirstCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /Outlines 4 0 R >>", PAGES, PAGE,
                "<< /Type /Outlines /First 5 0 R /Last 5 0 R /Count 1 >>",
                "<< /Title (A) /Parent 4 0 R /First 6 0 R >>",
                "<< /Title (B) /Parent 5 0 R /First 5 0 R >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            List<Map<String, Object>> bookmarks = SimpleBookmark.getBookmarkList(reader);

            assertThat(bookmarks).hasSize(1);
            assertThat(bookmarks.get(0)).containsEntry("Title", "A");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> kids = (List<Map<String, Object>>) bookmarks.get(0).get("Kids");
            assertThat(kids).hasSize(1);
            assertThat(kids.get(0)).containsEntry("Title", "B").doesNotContainKey("Kids");
        }
    }

    @Test
    void namedDestinationsAreConsolidatedDespiteOutlineCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /Outlines 4 0 R /Names << /Dests 7 0 R >> >>", PAGES, PAGE,
                "<< /Type /Outlines /First 5 0 R /Last 6 0 R /Count 2 >>",
                "<< /Title (A) /Parent 4 0 R /Dest (a) /Next 6 0 R /First 5 0 R >>",
                "<< /Title (B) /Parent 4 0 R /Next 5 0 R >>",
                "<< /Names [(a) [3 0 R /Fit]] >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            reader.consolidateNamedDestinations();

            PdfDictionary outline = (PdfDictionary) reader.getPdfObject(5);
            assertThat(outline.get(PdfName.DEST)).isInstanceOf(PdfArray.class);
        }
    }

    @Test
    void nameTreeStopsAtKidsCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /Names << /Dests 4 0 R >> >>", PAGES, PAGE,
                "<< /Kids [5 0 R 4 0 R 6 0 R] >>",
                "<< /Names [(a) [3 0 R /Fit]] >>",
                "<< /Kids [4 0 R 7 0 R] >>",
                "<< /Names [(b) [3 0 R /Fit]] >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(PdfNameTree.readTree((PdfDictionary) reader.getPdfObject(4)).keySet())
                    .containsExactlyInAnyOrder("a", "b");
            assertThat(reader.getNamedDestinationFromStrings().keySet()).containsExactlyInAnyOrder("a", "b");
        }
    }

    @Test
    void numberTreeStopsAtKidsCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /PageLabels 4 0 R >>", PAGES, PAGE,
                "<< /Kids [5 0 R 4 0 R] >>",
                "<< /Nums [0 << /S /r >>] >>");

        try (PdfReader reader = new PdfReader(pdf)) {
            assertThat(PdfNumberTree.readTree((PdfDictionary) reader.getPdfObject(4)).keySet()).containsExactly(0);
            assertThat(PdfPageLabels.getPageLabels(reader)).containsExactly("i");
        }
    }

    @Test
    void insertPageStopsAtParentCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R >>",
                "<< /Type /Pages /Kids [3 0 R] /Count 1 /Parent 2 0 R >>", PAGE);

        try (PdfReader reader = new PdfReader(pdf)) {
            PdfStamper stamper = new PdfStamper(reader, new ByteArrayOutputStream());
            stamper.insertPage(1, PageSize.A4);

            assertThat(reader.getNumberOfPages()).isEqualTo(2);
            stamper.close();
        }
    }

    @Test
    void calculationOrderStopsAtParentCycle() throws Exception {
        byte[] pdf = pdf("<< /Type /Catalog /Pages 2 0 R /AcroForm << /Fields [5 0 R] /CO [4 0 R] >> >>",
                PAGES, PAGE_WITH_WIDGET, WIDGET,
                "<< /T (a) /Kids [4 0 R] /Parent 6 0 R >>",
                "<< /T (b) /Parent 5 0 R >>");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (PdfReader reader = new PdfReader(pdf)) {
            PdfCopyFields copy = new PdfCopyFields(out);
            copy.addDocument(reader);
            copy.close();
        }

        assertThat(out.size()).isPositive();
    }

    /**
     * Builds a document from the given objects, which get the numbers 1, 2, 3 and so on.
     */
    private static byte[] pdf(String... objects) {
        StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
        int[] offsets = new int[objects.length];
        for (int i = 0; i < objects.length; i++) {
            offsets[i] = pdf.length();
            pdf.append(i + 1).append(" 0 obj\n").append(objects[i]).append("\nendobj\n");
        }
        int xref = pdf.length();
        pdf.append("xref\n0 ").append(objects.length + 1).append("\n0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.length + 1).append(" /Root 1 0 R >>\n")
                .append("startxref\n").append(xref).append("\n%%EOF\n");
        return pdf.toString().getBytes(ISO_8859_1);
    }
}
