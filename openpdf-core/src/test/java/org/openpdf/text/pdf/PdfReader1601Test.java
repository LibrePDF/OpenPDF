package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.junit.jupiter.api.Test;

/**
 * Regression test for <a href="https://github.com/LibrePDF/OpenPDF/issues/1601">issue #1601</a>:
 * a cross-reference stream whose compressed-object (type 2) entry points at an object-stream
 * number outside the document's actual object range used to make {@link PdfReader} throw a raw
 * {@link IndexOutOfBoundsException} instead of tolerating the corrupt reference.
 */
class PdfReader1601Test {

    @Test
    void corruptCompressedObjectReferenceDoesNotCrashPdfReader() throws Exception {
        byte[] pdf = createFullyCompressedPdf();
        byte[] corrupted = corruptFirstCompressedObjectReference(pdf);

        PdfReader reader = new PdfReader(corrupted);

        assertThat(reader.getNumberOfPages()).isEqualTo(1);
    }

    /**
     * Builds a PDF with {@code PdfWriter.setFullCompression()} so that it is parsed via a
     * cross-reference stream, with {@link Document#compress} disabled so that the
     * cross-reference stream bytes are stored raw and can be corrupted directly.
     */
    private byte[] createFullyCompressedPdf() throws Exception {
        boolean previousCompress = Document.compress;
        Document.compress = false;
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setFullCompression();
            document.open();
            document.add(new Paragraph("Hello #1601"));
            document.close();
            return out.toByteArray();
        } finally {
            Document.compress = previousCompress;
        }
    }

    /**
     * Finds the cross-reference stream via {@code startxref}, locates its first type 2
     * (compressed object) entry, and rewrites the object-stream number it points at so that it
     * is guaranteed to be outside the document's actual object range.
     */
    private byte[] corruptFirstCompressedObjectReference(byte[] pdf) {
        String ascii = new String(pdf, StandardCharsets.ISO_8859_1);

        int startxrefKeyword = ascii.lastIndexOf("startxref");
        Matcher offsetMatcher = Pattern.compile("startxref\\s+(\\d+)")
                .matcher(ascii.substring(startxrefKeyword));
        if (!offsetMatcher.find()) {
            throw new IllegalStateException("Could not find startxref offset in generated PDF");
        }
        int xrefStreamOffset = Integer.parseInt(offsetMatcher.group(1));

        int dictStart = ascii.indexOf("<<", xrefStreamOffset);
        int streamKeyword = ascii.indexOf("stream", dictStart);
        String dict = ascii.substring(dictStart, streamKeyword);
        int[] w = extractIntArray(dict, "W");

        int dataStart = streamKeyword + "stream".length();
        if (pdf[dataStart] == '\r') {
            dataStart++;
        }
        if (pdf[dataStart] == '\n') {
            dataStart++;
        }
        int endOfStream = ascii.indexOf("endstream", dataStart);

        byte[] corrupted = pdf.clone();
        int recordSize = w[0] + w[1] + w[2];
        for (int pos = dataStart; pos + recordSize <= endOfStream; pos += recordSize) {
            int type = readBigEndian(corrupted, pos, w[0]);
            if (type == 2) {
                int field2Offset = pos + w[0];
                for (int i = 0; i < w[1]; i++) {
                    corrupted[field2Offset + i] = (byte) 0xFF;
                }
                return corrupted;
            }
        }
        throw new IllegalStateException("No compressed (type 2) xref entry found to corrupt");
    }

    private static int[] extractIntArray(String dict, String key) {
        Matcher matcher = Pattern.compile("/" + key + "\\s*\\[([^]]*)]").matcher(dict);
        if (!matcher.find()) {
            throw new IllegalStateException("/" + key + " not found in xref stream dictionary: " + dict);
        }
        String[] parts = matcher.group(1).trim().split("\\s+");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            values[i] = Integer.parseInt(parts[i]);
        }
        return values;
    }

    private static int readBigEndian(byte[] data, int offset, int length) {
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | (data[offset + i] & 0xFF);
        }
        return value;
    }
}
