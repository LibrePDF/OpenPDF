package org.openpdf.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.*;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class Pdf20FeatureWriteAndVerifyTest {

    // Persistent output dir; not cleaned up by the test runner.
    private static final Path OUT_DIR = Path.of(System.getProperty("openpdf.outdir", "target/pdf20-tests"));

    @Test
    @DisplayName("Create PDF 2.0 to disk (no cleanup): header/version + Lang + AF + EmbeddedFiles + XMP + Unicode /UF")
    void createAndVerifyPdf20Features_persistent() throws Exception {
        Files.createDirectories(OUT_DIR);

        // -------- CREATE MAIN PDF --------
        Path outMain = OUT_DIR.resolve("pdf20-features.pdf");
        byte[] pdfBytes = createPdf20WithFeatures(outMain);
        System.out.println("Wrote: " + outMain.toAbsolutePath());

        // Header must be %PDF-2.0
        String header = new String(pdfBytes, 0, Math.min(4096, pdfBytes.length), StandardCharsets.ISO_8859_1);
        assertTrue(header.startsWith("%PDF-2.0"), "Header must be %PDF-2.0");

        // Verify via PdfReader
        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes))) {
            assertTrue(reader.getNumberOfPages() >= 1, "Expected at least one page");

            String readerVer = getVersionFromReaderNormalized(reader);
            if (readerVer != null) assertEquals("2.0", readerVer, "Reader version should be 2.0");

            PdfDictionary catalog = reader.getCatalog();
            assertNotNull(catalog, "Catalog missing");
            assertEquals("en-US", stripParens(catalog.getAsString(PdfName.LANG)), "Catalog /Lang mismatch");

            // /AF present with two attachments
            PdfArray afArr = catalog.getAsArray(new PdfName("AF"));
            assertNotNull(afArr, "/AF missing on Catalog");
            assertTrue(afArr.size() >= 2, "/AF should reference both attachments");

            // AF[0] AFRelationship=Data
            PdfDictionary fs1Dict = derefToDict(afArr.getAsIndirectObject(0));
            assertEquals(new PdfName("Data"), fs1Dict.get(new PdfName("AFRelationship")));

            // AF[1] has Unicode filename via /UF and AFRelationship=Data
            PdfDictionary fs2Dict = derefToDict(afArr.getAsIndirectObject(1));
            assertEquals(new PdfName("Data"), fs2Dict.get(new PdfName("AFRelationship")));
            PdfObject uf = fs2Dict.get(PdfName.UF);
            assertNotNull(uf, "Filespec #2 should contain /UF for Unicode filename");
            assertTrue(uf.toString().contains("les_meg_"), "Expected Unicode filename in /UF");

            // Names tree: EmbeddedFiles present
            PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
            assertNotNull(names, "/Names missing");
            PdfDictionary embeddedFiles = names.getAsDict(PdfName.EMBEDDEDFILES);
            assertNotNull(embeddedFiles, "/Names/EmbeddedFiles missing");
            PdfArray nameArray = embeddedFiles.getAsArray(PdfName.NAMES);
            assertNotNull(nameArray, "/Names/EmbeddedFiles/Names array missing");
            assertTrue(nameArray.size() >= 2, "EmbeddedFiles name tree should contain pairs");

            // XMP present
            byte[] xmp = reader.getMetadata();
            assertNotNull(xmp, "XMP metadata should be present");
            String xmpStr = new String(xmp, StandardCharsets.UTF_8);
            assertTrue(xmpStr.contains("<x:xmpmeta"), "XMP packet should look plausible");

            // Info.Title Unicode
            Map<String, String> info = reader.getInfo();
            assertTrue(info.containsKey("Title"));
            assertTrue(info.get("Title").contains("æ") && info.get("Title").contains("π"),
                    "Unicode Title should round-trip");
        }

        // -------- WRITE A STAMPED VARIANT WITH PAGE-LEVEL /Lang OVERRIDE (ALSO NOT DELETED) --------
        Path outStamped = OUT_DIR.resolve("pdf20-features-lang-override.pdf");
        try (PdfReader reader = new PdfReader(outMain.toString());
                OutputStream os = Files.newOutputStream(outStamped)) {
            PdfStamper stamper = new PdfStamper(reader, os);
            PdfDictionary page1 = reader.getPageN(1);
            page1.put(PdfName.LANG, new PdfString("nb-NO"));
            stamper.close();
        }
        System.out.println("Wrote: " + outStamped.toAbsolutePath());

        try (PdfReader r2 = new PdfReader(outStamped.toString())) {
            assertEquals("en-US", stripParens(r2.getCatalog().getAsString(PdfName.LANG)),
                    "Catalog /Lang should remain en-US");
            assertEquals("nb-NO", stripParens(r2.getPageN(1).getAsString(PdfName.LANG)),
                    "Page 1 /Lang override should be nb-NO");
        }

        System.out.println("Output directory: " + OUT_DIR.toAbsolutePath());
        System.out.println("Done. Files are NOT deleted.");
    }

    // -------- helpers --------

    private static byte[] createPdf20WithFeatures(Path out) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Document doc = new Document();
        PdfWriter writer;
        try (OutputStream fos = Files.newOutputStream(out)) {
            writer = PdfWriter.getInstance(doc, new TeeOutputStream(baos, fos));
            writer.setXmpMetadata(minimalXmp("en-US", "OpenPDF 2.0 feature test").getBytes(StandardCharsets.UTF_8));

            doc.open();

            // Catalog /Lang
            writer.getExtraCatalog().put(PdfName.LANG, new PdfString("en-US"));

            // Content
            doc.add(new Paragraph("Hello PDF 2.0 – " + Instant.now()));

            // Attachment #1 (AFRelationship=Data)
            byte[] bytes1 = "Hello attachment #1".getBytes(StandardCharsets.UTF_8);
            PdfFileSpecification fs1 = PdfFileSpecification.fileEmbedded(writer, null, "readme.txt", bytes1);
            fs1.put(new PdfName("AFRelationship"), new PdfName("Data"));
            writer.addFileAttachment(fs1);

            // Attachment #2 with Unicode filename via /UF
            byte[] bytes2 = "Hello attachment #2 (UTF-16 filename)".getBytes(StandardCharsets.UTF_8);
            String unicodeName = "les_meg_æøå.txt";
            PdfFileSpecification fs2 = PdfFileSpecification.fileEmbedded(writer, null, unicodeName, bytes2);
            fs2.put(PdfName.UF, new PdfString(unicodeName, PdfObject.TEXT_UNICODE));
            fs2.put(new PdfName("AFRelationship"), new PdfName("Data"));
            writer.addFileAttachment(fs2);

            // /AF array referencing both
            PdfArray af = new PdfArray();
            af.add(fs1.getReference());
            af.add(fs2.getReference());
            writer.getExtraCatalog().put(new PdfName("AF"), af);

            // Unicode Title (Info)
            doc.addTitle("Tittel æøå – π");

            doc.close(); // closes writer & flushes both streams
        }

        return baos.toByteArray();
    }

    /** Minimal XMP packet (UTF-8). */
    private static String minimalXmp(String lang, String dcTitle) {
        return ""
                + "<?xpacket begin=\"\uFEFF\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>"
                + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">"
                + "  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""
                + "           xmlns:dc=\"http://purl.org/dc/elements/1.1/\">"
                + "    <rdf:Description>"
                + "      <dc:title><rdf:Alt>"
                + "        <rdf:li xml:lang=\"" + lang + "\">" + escapeXml(dcTitle) + "</rdf:li>"
                + "      </rdf:Alt></dc:title>"
                + "    </rdf:Description>"
                + "  </rdf:RDF>"
                + "</x:xmpmeta>"
                + "<?xpacket end=\"w\"?>";
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Normalize PdfReader#getPdfVersion(): '4' -> "1.4", "2.0" -> "2.0". May return null. */
    private static String getVersionFromReaderNormalized(PdfReader reader) {
        try {
            Method m = reader.getClass().getMethod("getPdfVersion");
            Object v = m.invoke(reader);
            if (v == null) return null;
            if (v instanceof Character) {
                char c = (Character) v;
                if (Character.isDigit(c)) return "1." + c;
                return String.valueOf(c);
            }
            if (v instanceof String) return (String) v;
        } catch (NoSuchMethodException ignored) {
            // OK: header check already covers us
        } catch (Exception e) {
            System.out.println("getPdfVersion() reflection error: " + e);
        }
        return null;
    }

    private static String stripParens(PdfString s) {
        return s == null ? null : s.toString().replaceAll("[()]", "");
    }

    private static PdfDictionary derefToDict(PdfIndirectReference ref) {
        if (ref == null) return null;
        PdfObject obj = PdfReader.getPdfObject(ref);
        return (obj instanceof PdfDictionary) ? (PdfDictionary) obj : null;
    }

    /** Writes to two streams at once (memory + file). */
    private static final class TeeOutputStream extends OutputStream {
        private final OutputStream a, b;
        TeeOutputStream(OutputStream a, OutputStream b) { this.a = a; this.b = b; }
        @Override public void write(int i) throws IOException { a.write(i); b.write(i); }
        @Override public void write(byte[] buf) throws IOException { a.write(buf); b.write(buf); }
        @Override public void write(byte[] buf, int off, int len) throws IOException { a.write(buf, off, len); b.write(buf, off, len); }
        @Override public void flush() throws IOException { a.flush(); b.flush(); }
        @Override public void close() throws IOException { try { a.close(); } finally { b.close(); } }
    }
}
