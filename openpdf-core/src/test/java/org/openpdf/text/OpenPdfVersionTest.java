package org.openpdf.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class OpenPdfVersionTest {

    private static final Set<String> SUPPORTED_VERSIONS = buildSupportedVersions();

    private static Set<String> buildSupportedVersions() {
        Set<String> s = new LinkedHashSet<>();
        for (int i = 0; i <= 7; i++) s.add("1." + i);
        s.add("2.0");
        return s;
    }

    // Directory with sample PDFs to read (override with -Dopenpdf.test.pdfdir=/path)
    private static final Path SAMPLE_DIR = Path.of(
            System.getProperty("openpdf.test.pdfdir", "src/test/resources"));

    // Put these files in SAMPLE_DIR
    private static final String[] SAMPLE_FILES = {
            "EmptyPage.pdf",
            "HelloWorldMeta.pdf",
            "OutlineUriActionWithNoTitle.pdf",
            "SimulatedBoldAndStrokeWidth.pdf",
            "barcode_macro_pdf_417.pdf",
            "encodingTest.pdf",
            "identity-h.pdf",
            "merge-acroforms.pdf",
            "objectXref.pdf",
            "open_protected.pdf",                    // may be encrypted
            "openpdf_bug_test.pdf",
            "pades_infinite_loop.pdf",
            "pades_opposite_infinite_loop.pdf",
            "pdf_digital_signature_timestamp.pdf",
            "pdf_form_metadata_issue_254.pdf",
            "pdfsmartcopy_bec.pdf",
            "sample_signed-sha1.pdf",
            "sample_signed-sha512.pdf",
            "siwa.pdf"
    };

    // ============== CREATE NEW PDFs: DEFAULT + SPECIFIC VERSIONS ==============

    @Test
    @DisplayName("Creating a new PDF without setting version defaults to 2.0 (header and reader)")
    void createDefaultPdfIs20(@TempDir Path tmp) throws Exception {
        // In-memory
        byte[] bytes = createPdfToBytes(null);
        assertHeaderAndReaderVersion("in-memory (default)", bytes, "2.0");

        // Also write to disk for sanity
        Path out = tmp.resolve("default.pdf");
        createPdfToFile(null, out);
        byte[] bytesOnDisk = Files.readAllBytes(out);
        assertHeaderAndReaderVersion("file:" + out.getFileName(), bytesOnDisk, "2.0");
    }

    @ParameterizedTest(name = "Create + round-trip version {0} (memory and file)")
    @ValueSource(strings = {"1.2","1.3","1.4","1.5","1.6","1.7","2.0"})
    void createAndRoundTripSpecificVersions(String version, @TempDir Path tmp) throws Exception {
        // In-memory
        byte[] bytes = createPdfToBytes(version);
        assertHeaderAndReaderVersion("in-memory " + version, bytes, version);

        // To file
        Path out = tmp.resolve("doc-" + version.replace('.', '_') + ".pdf");
        createPdfToFile(version, out);
        byte[] bytesOnDisk = Files.readAllBytes(out);
        assertHeaderAndReaderVersion("file:" + out.getFileName(), bytesOnDisk, version);
    }

    @Test
    @DisplayName("Legacy char-based setter (if present) maps '4' to 1.4, etc.")
    void legacyCharSetterIfPresent(@TempDir Path tmp) throws Exception {
        // Only run if PdfWriter has setPdfVersion(char)
        Method charSetter = findCharSetterIfPresent();
        if (charSetter == null) {
            System.out.println("PdfWriter#setPdfVersion(char) not present; skipping legacy-char test.");
            return;
        }

        // Create using char '4' => expect %PDF-1.4
        byte[] bytes = createPdfToBytesWithCharSetter('4');
        assertHeaderAndReaderVersion("in-memory char '4'", bytes, "1.4");

        // File variant
        Path out = tmp.resolve("legacy_char_1_4.pdf");
        createPdfToFileWithCharSetter('4', out);
        byte[] onDisk = Files.readAllBytes(out);
        assertHeaderAndReaderVersion("file:" + out.getFileName(), onDisk, "1.4");
    }

    // ============== READ EXISTING PDFs (FROM RESOURCES) ==============

    @Test
    @DisplayName("Read existing PDFs: log header & reader versions; open when possible")
    void readExistingPdfsAndVerifyVersion() throws IOException {
        assertTrue(Files.isDirectory(SAMPLE_DIR),
                "Sample directory not found: " + SAMPLE_DIR.toAbsolutePath() +
                        " (override with -Dopenpdf.test.pdfdir=/path/to/dir)");

        for (String name : SAMPLE_FILES) {
            Path p = SAMPLE_DIR.resolve(name);
            assertTrue(Files.isRegularFile(p), "Missing test file: " + p.toAbsolutePath());

            byte[] bytes = Files.readAllBytes(p);

            // Header version must parse and be among supported
            String headerVersion = extractHeaderVersion(bytes);
            assertTrue(headerVersion.matches("\\d+\\.\\d+"),
                    () -> name + ": header version not x.y: " + headerVersion);
            assertTrue(SUPPORTED_VERSIONS.contains(headerVersion),
                    () -> name + ": unexpected header version " + headerVersion +
                            " (allowed: " + SUPPORTED_VERSIONS + ")");

            // Try to open and get reader version (normalize if needed)
            try (PdfReader reader = new PdfReader(new ByteArrayInputStream(bytes))) {
                int pages = reader.getNumberOfPages();
                assertTrue(pages >= 1, () -> name + ": reader reports zero pages");

                String readerVersion = getVersionFromReaderNormalized(reader); // may be null
                System.out.printf("PDF %-35s | header=%-4s | reader=%s | pages=%d%n",
                        name, headerVersion, (readerVersion == null ? "(n/a)" : readerVersion), pages);

                if (readerVersion != null) {
                    assertEquals(headerVersion, readerVersion,
                            () -> name + ": reader version should match header");
                }


            } catch (Exception openEx) {
                // Likely encrypted/signed. Acceptable as long as header is OK.
                String msg = String.valueOf(openEx.getMessage()).toLowerCase();
                boolean looksEncrypted = msg.contains("password") || msg.contains("encrypted");
                boolean namedProtected = name.toLowerCase().contains("protected") ||
                        name.toLowerCase().contains("signed");
                System.out.printf("PDF %-35s | header=%-4s | reader=(failed to open: %s)%n",
                        name, headerVersion, openEx.getClass().getSimpleName());
                assertTrue(looksEncrypted || namedProtected,
                        () -> name + ": failed to open (not clearly due to encryption): " + openEx);
            }
        }
    }

    // ============== HELPERS ==============

    private static byte[] createPdfToBytes(String version) throws Exception {
        Document doc = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        if (version != null && !version.isEmpty()) {
            setVersion(writer, version); // supports String or char-based fallbacks
        }

        doc.open();
        doc.add(new Paragraph("Hello version " + (version == null ? "(default)" : version)));
        doc.close();

        return baos.toByteArray();
    }

    private static void createPdfToFile(String version, Path out) throws Exception {
        Files.createDirectories(out.getParent());
        Document doc = new Document();
        try (OutputStream os = Files.newOutputStream(out)) {
            PdfWriter writer = PdfWriter.getInstance(doc, os);

            if (version != null && !version.isEmpty()) {
                setVersion(writer, version);
            }

            doc.open();
            doc.add(new Paragraph("Hello version " + (version == null ? "(default)" : version)));
            doc.close();
        }
    }

    /** Use legacy char-based setter if present for 1.x. */
    private static byte[] createPdfToBytesWithCharSetter(char verDigit) throws Exception {
        Document doc = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, baos);

        Method m = findCharSetterIfPresent();
        assertNotNull(m, "setPdfVersion(char) not present");
        m.invoke(writer, verDigit);

        doc.open();
        doc.add(new Paragraph("Hello legacy char " + verDigit));
        doc.close();

        return baos.toByteArray();
    }

    private static void createPdfToFileWithCharSetter(char verDigit, Path out) throws Exception {
        Files.createDirectories(out.getParent());
        Document doc = new Document();
        try (OutputStream os = Files.newOutputStream(out)) {
            PdfWriter writer = PdfWriter.getInstance(doc, os);

            Method m = findCharSetterIfPresent();
            assertNotNull(m, "setPdfVersion(char) not present");
            m.invoke(writer, verDigit);

            doc.open();
            doc.add(new Paragraph("Hello legacy char " + verDigit));
            doc.close();
        }
    }

    /** Prefer String setter; if absent, fall back to char for 1.x; else throw. */
    private static void setVersion(PdfWriter writer, String version) throws Exception {
        // try String setter first
        try {
            Method m = writer.getClass().getMethod("setPdfVersion", String.class);
            m.invoke(writer, version);
            return;
        } catch (NoSuchMethodException ignored) {
            // fall back
        }

        // char-based fallback for 1.x
        if (version.matches("1\\.[0-7]")) {
            char last = version.charAt(version.length() - 1);
            Method m = writer.getClass().getMethod("setPdfVersion", char.class);
            m.invoke(writer, last);
            return;
        }

        throw new UnsupportedOperationException(
                "PdfWriter#setPdfVersion(String) not found and char fallback not applicable for: " + version);
    }

    /** Normalize reader version to "x.y" if possible (e.g., '4' -> "1.4"). */
    private static String getVersionFromReaderNormalized(PdfReader reader) {
        try {
            // Common in OpenPDF: getPdfVersion() returns char or String
            Method m = reader.getClass().getMethod("getPdfVersion");
            Object v = m.invoke(reader);
            if (v == null) return null;
            if (v instanceof Character) {
                char c = (Character) v;
                if (Character.isDigit(c)) {
                    // Legacy semantics: '2'..'7' mean 1.2..1.7
                    return "1." + c;
                }
                return String.valueOf(c);
            }
            if (v instanceof String) {
                return (String) v; // e.g., "1.7" or "2.0"
            }
        } catch (NoSuchMethodException ignored) {
            // try alternate name if your fork differs
            try {
                Method m2 = reader.getClass().getMethod("getHeaderVersion");
                Object v2 = m2.invoke(reader);
                if (v2 instanceof String) return (String) v2;
            } catch (Exception ignored2) {
                // no suitable method
            }
        } catch (Exception e) {
            System.out.println("getVersionFromReaderNormalized error: " + e);
        }
        return null;
    }

    /** Assert that header equals expected and matches reader (normalized), logging details. */
    private static void assertHeaderAndReaderVersion(String label, byte[] bytes, String expected) throws Exception {
        String headerVersion = extractHeaderVersion(bytes);
        assertEquals(expected, headerVersion, label + ": header version mismatch");

        try (PdfReader reader = new PdfReader(new ByteArrayInputStream(bytes))) {
            int pages = reader.getNumberOfPages();
            assertTrue(pages >= 1, label + ": expected at least one page");

            String readerVersion = getVersionFromReaderNormalized(reader);
            System.out.printf("CREATED %-28s | header=%-4s | reader=%s | pages=%d%n",
                    label, headerVersion, (readerVersion == null ? "(n/a)" : readerVersion), pages);

            if (readerVersion != null) {
                assertEquals(headerVersion, readerVersion,
                        label + ": reader version should match header");
            }
        }
    }

    /**
     * Extracts the "%PDF-x.y" header version string from a PDF byte array.
     * Returns e.g. "1.7" or "2.0".
     */
    private static String extractHeaderVersion(byte[] pdfBytes) {
        int len = Math.min(pdfBytes.length, 4096);
        String header = new String(pdfBytes, 0, len, StandardCharsets.ISO_8859_1);
        int idx = header.indexOf("%PDF-");
        if (idx < 0) throw new IllegalStateException("PDF header not found");
        int start = idx + 5;
        int end = start;
        while (end < header.length()) {
            char c = header.charAt(end);
            if (!Character.isDigit(c) && c != '.') break;
            end++;
        }
        return header.substring(start, end);
    }

    private static Method findCharSetterIfPresent() {
        try {
            return PdfWriter.class.getMethod("setPdfVersion", char.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

}
