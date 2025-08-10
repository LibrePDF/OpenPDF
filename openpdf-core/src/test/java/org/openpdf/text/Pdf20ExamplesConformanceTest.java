package org.openpdf.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Pdf20ExamplesConformanceTest {

    // Override with -Dopenpdf.pdf20.dir=/absolute/path if needed
    private static final Path DIR = Path.of(System.getProperty("openpdf.pdf20.dir", "src/test/resources/pdf-2-0"));
    private static Path f(String name) { return DIR.resolve(name); }

    // ---------- Simple PDF 2.0 file ----------

    @Test
    @DisplayName("Simple PDF 2.0 file.pdf → contains a 2.0 header, openable, ≥1 page")
    void testSimplePdf20() throws Exception {
        Path p = f("Simple PDF 2.0 file.pdf");
        byte[] bytes = Files.readAllBytes(p);
        assertTrue(containsHeaderVersion(bytes, "2.0"), "File must contain a %PDF-2.0 header");
        try (PdfReader r = new PdfReader(new ByteArrayInputStream(bytes))) {
            assertTrue(r.getNumberOfPages() >= 1, "Expected at least one page");
        }
    }

    // ---------- UTF-8 stress test ----------

    @Test
    @DisplayName("pdf20-utf8-test.pdf → contains 2.0 header, openable, likely non-ASCII metadata present")
    void testUtf8Test() throws Exception {
        Path p = f("pdf20-utf8-test.pdf");
        byte[] bytes = Files.readAllBytes(p);
        assertTrue(containsHeaderVersion(bytes, "2.0"), "File must contain a %PDF-2.0 header");

        try (PdfReader r = new PdfReader(new ByteArrayInputStream(bytes))) {
            assertTrue(r.getNumberOfPages() >= 1, "Expected at least one page");
            var info = r.getInfo();
            boolean hasNonAscii = info.values().stream().filter(v -> v != null)
                    .anyMatch(v -> v.codePoints().anyMatch(cp -> cp > 127));
            assertTrue(hasNonAscii, "Expected at least one non-ASCII Info value");
        }
    }

    // ---------- UTF-8 string inside an annotation ----------

    @Test
    @DisplayName("PDF 2.0 UTF-8 string and annotation.pdf → annotation /Contents has non-ASCII")
    void testUtf8Annotation() throws Exception {
        Path p = f("PDF 2.0 UTF-8 string and annotation.pdf");
        byte[] bytes = Files.readAllBytes(p);
        assertTrue(containsHeaderVersion(bytes, "2.0"), "File must contain a %PDF-2.0 header");

        try (PdfReader r = new PdfReader(new ByteArrayInputStream(bytes))) {
            PdfDictionary page1 = r.getPageN(1);
            PdfArray annots = page1.getAsArray(PdfName.ANNOTS);
            assertNotNull(annots, "Expected Annots array on page 1");
            boolean foundNonAscii = false;

            for (int i = 0; i < annots.size(); i++) {
                PdfDictionary a = (PdfDictionary) PdfReader.getPdfObject(annots.getAsIndirectObject(i));
                if (a == null) continue;
                PdfString contents = a.getAsString(PdfName.CONTENTS);
                if (contents != null) {
                    String s = contents.toUnicodeString();
                    if (s != null && s.codePoints().anyMatch(cp -> cp > 127)) {
                        foundNonAscii = true;
                        break;
                    }
                }
            }
            assertTrue(foundNonAscii, "Expected a non-ASCII UTF-8 annotation /Contents");
        }
    }


    // ---------- Offset start (PDF data not at byte 0) ----------

    @Test
    @DisplayName("PDF 2.0 with offset start.pdf → first header is at positive offset and is 2.0")
    void testOffsetStart() throws Exception {
        Path p = f("PDF 2.0 with offset start.pdf");
        byte[] bytes = Files.readAllBytes(p);

        Header occ = firstHeader(bytes);
        assertNotNull(occ, "No %PDF- header found");
        assertTrue(occ.offset > 0, "Expected %PDF- header to appear at a positive offset");
        assertEquals("2.0", occ.version, "First header should be 2.0");

        try (PdfReader r = new PdfReader(new ByteArrayInputStream(bytes))) {
            assertTrue(r.getNumberOfPages() >= 1);
        }
    }

    // ---------- Page-level OutputIntent (new in 2.0) ----------

    @Test
    @DisplayName("PDF 2.0 with page level output intent.pdf → page 1 has /OutputIntents")
    void testPageLevelOutputIntent() throws Exception {
        Path p = f("PDF 2.0 with page level output intent.pdf");
        byte[] bytes = Files.readAllBytes(p);
        assertTrue(containsHeaderVersion(bytes, "2.0"), "File must contain a %PDF-2.0 header");

        try (PdfReader r = new PdfReader(new ByteArrayInputStream(bytes))) {
            PdfDictionary catalog = r.getCatalog();
            PdfArray docOI = catalog.getAsArray(PdfName.OUTPUTINTENTS);
            assertNotNull(docOI, "Expected document-level /OutputIntents");

            PdfDictionary page1 = r.getPageN(1);
            PdfArray pageOI = page1.getAsArray(PdfName.OUTPUTINTENTS);
            assertNotNull(pageOI, "Expected page-level /OutputIntents on page 1");
            assertTrue(pageOI.size() >= 1, "Page-level /OutputIntents should not be empty");
        }
    }

    // ================= helpers =================

    private static class Header {
        final int offset; final String version;
        Header(int o, String v) { offset = o; version = v; }
    }

    /** Find the first %PDF- header anywhere and parse its version. */
    private static Header firstHeader(byte[] bytes) {
        int idx = indexOfAscii(bytes, "%PDF-");
        if (idx < 0) return null;
        String ver = parseHeaderVersion(bytes, idx);
        return new Header(idx, ver);
    }

    /** True if the file contains a %PDF-x.y header with the given version anywhere. */
    private static boolean containsHeaderVersion(byte[] bytes, String wanted) {
        for (Header h : allHeaders(bytes)) {
            if (wanted.equals(h.version)) return true;
        }
        return false;
    }

    /** Return all %PDF- headers with their offsets and versions. */
    private static List<Header> allHeaders(byte[] bytes) {
        List<Header> list = new ArrayList<>();
        int from = 0;
        byte[] pat = "%PDF-".getBytes(StandardCharsets.ISO_8859_1);
        while (true) {
            int idx = indexOf(bytes, pat, from);
            if (idx < 0) break;
            String ver = parseHeaderVersion(bytes, idx);
            list.add(new Header(idx, ver));
            from = idx + pat.length;
        }
        return list;
    }

    /** Parse "x.y" after "%PDF-" starting at headerIdx. */
    private static String parseHeaderVersion(byte[] bytes, int headerIdx) {
        int start = headerIdx + 5;
        int end = start;
        while (end < bytes.length) {
            char c = (char) (bytes[end] & 0xFF);
            if (!Character.isDigit(c) && c != '.') break;
            end++;
        }
        return new String(bytes, start, end - start, StandardCharsets.ISO_8859_1);
    }

    private static int countAscii(byte[] bytes, String token) {
        int count = 0, idx = 0;
        byte[] pat = token.getBytes(StandardCharsets.ISO_8859_1);
        while ((idx = indexOf(bytes, pat, idx)) >= 0) {
            count++;
            idx += pat.length;
        }
        return count;
    }

    private static int indexOfAscii(byte[] bytes, String token) {
        return indexOf(bytes, token.getBytes(StandardCharsets.ISO_8859_1), 0);
    }

    private static int indexOf(byte[] hay, byte[] needle, int from) {
        outer: for (int i = from; i <= hay.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (hay[i + j] != needle[j]) continue outer;
            }
            return i;
        }
        return -1;
    }
}
