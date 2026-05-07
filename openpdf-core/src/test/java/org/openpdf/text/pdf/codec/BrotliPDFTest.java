/*
 * Copyright 2026 OpenPDF
 *
 * Licensed under the LGPL 2.1 or MPL 2.0.
 */
package org.openpdf.text.pdf.codec;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.openpdf.text.Document;
import org.openpdf.text.Paragraph;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Tests for Brotli compression support in OpenPDF: codec round-trip,
 * round-trip through a PDF whose page content is written with Brotli, and
 * reading an externally-produced PDF that uses the {@code /BrotliDecode} filter.
 */
class BrotliPDFTest {

    private static final String EXTERNAL_BROTLI_PDF = "/Brotli-Prototype-FileA.pdf";

    @Test
    void roundTripCompressDecompress() throws Exception {
        byte[] original = ("OpenPDF Brotli round-trip test. "
                + "The quick brown fox jumps over the lazy dog. ").repeat(50)
                .getBytes(StandardCharsets.UTF_8);

        byte[] compressed = BrotliFilter.encode(original);
        assertThat(compressed).isNotEmpty();
        assertThat(compressed.length).isLessThan(original.length);

        byte[] decoded = BrotliFilter.decode(compressed);
        assertThat(decoded).isEqualTo(original);
    }

    /**
     * Writes a PDF with Brotli-compressed page content streams via {@code PdfWriter},
     * then reads it back and verifies the page content stream uses
     * {@code /BrotliDecode} and decodes correctly.
     */
    @Test
    void pdfWithBrotliCompressedStreamCanBeRead() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        writer.setUseBrotliCompression(true);
        document.open();
        document.add(new Paragraph(
                "Hello Brotli! This page content stream is compressed with Brotli."));
        document.close();

        // Sanity: the output PDF advertises the /BrotliDecode filter for the page content.
        String raw = new String(baos.toByteArray(), StandardCharsets.ISO_8859_1);
        assertThat(raw).contains("/BrotliDecode");

        PdfReader reader = new PdfReader(baos.toByteArray());
        try {
            assertThat(reader.getNumberOfPages()).isEqualTo(1);

            // The page content stream must use /BrotliDecode and decode to text containing
            // the standard PDF text-showing operator "Tj" or "TJ".
            byte[] pageContent = reader.getPageContent(1);
            assertThat(pageContent).isNotEmpty();
            String pageContentText = new String(pageContent, StandardCharsets.ISO_8859_1);
            assertThat(pageContentText).containsAnyOf("Tj", "TJ");

            // Confirm the underlying stream is actually labeled /BrotliDecode.
            int brotliLabeled = 0;
            for (int i = 1; i < reader.getXrefSize(); i++) {
                PdfObject obj = reader.getPdfObject(i);
                if (!(obj instanceof PRStream prs)) {
                    continue;
                }
                PdfObject filter = PdfReader.getPdfObject(prs.get(PdfName.FILTER));
                if (filter != null && filter.toString().contains("BrotliDecode")) {
                    brotliLabeled++;
                    // Each such stream must decode without error.
                    assertThat(PdfReader.getStreamBytes(prs)).isNotNull();
                }
            }
            assertThat(brotliLabeled)
                    .as("at least one stream must be labeled /BrotliDecode")
                    .isGreaterThan(0);
        } finally {
            reader.close();
        }
    }

    /**
     * Reads an externally-produced PDF whose streams use the {@code /BrotliDecode}
     * filter and verifies that every such stream can be decompressed by OpenPDF and
     * that the resulting document is structurally sound.
     * <p>
     * The fixture {@code Brotli-Prototype-FileA.pdf} is a PDF 2.0 file produced by
     * AutoCAD's {@code pdfplot11.hdi} where every stream (xref stream, object stream,
     * page content stream, font / image streams) is Brotli-compressed.
     */
    @Test
    void canReadExternalBrotliCompressedPdf() throws Exception {
        byte[] pdfBytes;
        try (var in = BrotliPDFTest.class.getResourceAsStream(EXTERNAL_BROTLI_PDF)) {
            assertThat(in)
                    .as("test resource %s must exist", EXTERNAL_BROTLI_PDF)
                    .isNotNull();
            pdfBytes = in.readAllBytes();
        }

        // The fixture is a PDF 2.0 file whose streams are all Brotli-compressed.
        String raw = new String(pdfBytes, StandardCharsets.ISO_8859_1);
        assertThat(raw).startsWith("%PDF-2.0");
        assertThat(raw)
                .as("fixture PDF should contain at least one /BrotliDecode filter")
                .contains("/BrotliDecode");

        PdfReader reader = new PdfReader(pdfBytes);
        try {
            // Document-level structure must parse correctly. The catalog and trailer
            // both live inside Brotli-compressed object / xref streams.
            assertThat(reader.getPdfVersion()).isEqualTo("2.0");
            assertThat(reader.getNumberOfPages()).isEqualTo(25);

            // Producer / Title come from the document info dictionary, which lives
            // inside a Brotli-compressed object stream in this fixture.
            var info = reader.getInfo();
            assertThat(info.get("Producer")).isEqualTo("pdfplot11.hdi 11.1.18.0");
            assertThat(info.get("Title")).isEqualTo("A5.0");
            assertThat(info.get("Creator")).contains("AutoCAD");

            // Page 1's content stream must decode to real PDF content. AutoCAD plots
            // are vector drawings, so we expect path operators rather than text.
            byte[] pageContent = reader.getPageContent(1);
            assertThat(pageContent)
                    .as("decoded page 1 content stream")
                    .isNotEmpty();
            String pageContentText = new String(pageContent, StandardCharsets.ISO_8859_1);
            assertThat(pageContentText)
                    .as("page 1 content should contain PDF graphics-state operators")
                    .containsAnyOf(" q\n", " Q\n", " cm\n", " m\n", " l\n", " S\n", " re\n", "BT", "ET");

            // Every page in the document must have a non-empty, decodable content stream.
            for (int p = 1; p <= reader.getNumberOfPages(); p++) {
                assertThat(reader.getPageContent(p))
                        .as("decoded content stream for page %d", p)
                        .isNotEmpty();
            }

            // Walk the xref table: every Brotli-labeled stream must decode without
            // error, and the total decoded payload must be considerably larger than
            // the compressed file (Brotli must actually have compressed something).
            int brotliStreamCount = 0;
            long totalDecodedBytes = 0;
            for (int i = 1; i < reader.getXrefSize(); i++) {
                PdfObject obj = reader.getPdfObject(i);
                if (!(obj instanceof PRStream prs)) {
                    continue;
                }
                PdfObject filter = PdfReader.getPdfObject(prs.get(PdfName.FILTER));
                if (filter == null || !filter.toString().contains("BrotliDecode")) {
                    continue;
                }
                byte[] decoded = PdfReader.getStreamBytes(prs);
                assertThat(decoded)
                        .as("decoded Brotli stream #%d", i)
                        .isNotNull()
                        .isNotEmpty();
                brotliStreamCount++;
                totalDecodedBytes += decoded.length;
            }

            assertThat(brotliStreamCount)
                    .as("the fixture must contain many /BrotliDecode streams")
                    .isGreaterThanOrEqualTo(20);
            assertThat(totalDecodedBytes)
                    .as("total decoded payload must exceed the compressed file size")
                    .isGreaterThan(pdfBytes.length);
        } finally {
            reader.close();
        }
    }
}

