package org.openpdf.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PDF 2.0–focused smoke tests for OpenPDF.
 * Covers header/version, AF (Associated Files), Lang, XMP, and Unicode Info.
 */
class Pdf20ComplianceTest {

    @Test
    @DisplayName("PDF 2.0: header, AF, Lang, XMP, Unicode Title")
    void createAndReadPdf20() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        // (Optional but fine) XMP can be set before or after open; keeping before open is OK
        writer.setXmpMetadata(minimalXmp("en-US", "OpenPDF 2.0 test").getBytes(StandardCharsets.UTF_8));

        // Open FIRST → initializes writer.body
        doc.open();

        // Now it's safe to touch structures that write to the body
        // /Lang on catalog (good hygiene for 2.0)
        writer.getExtraCatalog().put(PdfName.LANG, new PdfString("en-US"));

        // Add content
        doc.add(new Paragraph("Hello PDF 2.0!"));

        // Embedded file + AF (AFRelationship=Data)
        byte[] embedded = "Hello attachment".getBytes(StandardCharsets.UTF_8);
        PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(writer, null, "readme.txt", embedded);
        fs.put(new PdfName("AFRelationship"), new PdfName("Data"));
        writer.addFileAttachment(fs);

        PdfArray afArray = new PdfArray();
        afArray.add(fs.getReference());
        writer.getExtraCatalog().put(new PdfName("AF"), afArray);

        // Title with Unicode (will live in Info; OK for this smoke test)
        doc.addTitle("Tittel æøå – π");

        doc.close();

        byte[] pdfBytes = baos.toByteArray();

        // Quick asserts (examples)
        String header = new String(pdfBytes, 0, Math.min(4096, pdfBytes.length), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF-2.0"), "Header must be %PDF-2.0");

        try (PdfReader reader = new PdfReader(new java.io.ByteArrayInputStream(pdfBytes))) {
            assertTrue(reader.getNumberOfPages() >= 1);

            // Reader version (normalize if char)
            String readerVer;
            try {
                Object v = PdfReader.class.getMethod("getPdfVersion").invoke(reader);
                readerVer = (v instanceof Character) ? ("1." + v) : String.valueOf(v);
            } catch (NoSuchMethodException e) {
                readerVer = null; // older fork without the method; header check already done
            }
            if (readerVer != null) {
                assertEquals("2.0", readerVer);
            }

            PdfDictionary catalog = reader.getCatalog();
            assertEquals("en-US", catalog.getAsString(PdfName.LANG).toString().replaceAll("[()]", ""));

            PdfArray af = catalog.getAsArray(new PdfName("AF"));
            assertNotNull(af);
            PdfDictionary fsDict = (PdfDictionary) PdfReader.getPdfObject(af.getAsIndirectObject(0));
            assertEquals(new PdfName("Data"), fsDict.get(new PdfName("AFRelationship")));
        }
    }

    // ---------------- helpers ----------------

    /** Minimal but valid XMP packet (UTF-8). */
    private static String minimalXmp(String lang, String dcTitle) {
        return ""
                + "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>"
                + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">"
                + "  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
                + "           xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
                + "    <rdf:Description>"
                + "      <dc:title>"
                + "        <rdf:Alt>"
                + "          <rdf:li xml:lang=\"" + lang + "\">" + escapeXml(dcTitle) + "</rdf:li>"
                + "        </rdf:Alt>"
                + "      </dc:title>"
                + "    </rdf:Description>"
                + "  </rdf:RDF>"
                + "</x:xmpmeta>"
                + "<?xpacket end=\"w\"?>";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

}
