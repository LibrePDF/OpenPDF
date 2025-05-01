package com.lowagie.text.pdf;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class LZWDecoderTest {

    @Test
    void shouldDecodeType4PSCalcFunction1() throws IOException {
        // Test verifies fix for issue #1298 where LZW decoder gets lost when encoded data does not start with Clear
        // Table (256) code, which appears to happen with the short, text-based PostScript calculator functions in
        // colorspaces.

        // This specific sample causes garbled output from LZWDecoder before the fix for issue #1298.
        assertDoesNotThrow(() -> testLzwDecoder(
                "/issue1298/lzw-ps-function-1-encoded.bin",
                "/issue1298/lzw-ps-function-1-decoded.txt"));
    }

    @Test
    void shouldDecodeType4PSCalcFunction2() {
        // This specific sample is also an LZW encoded Type 4 PostScript calculator function that does not start with
        // LZW Clear Table code.  This data caused the LZWDecoder to throw a null exception before the fix for #1298.
        assertDoesNotThrow(() -> testLzwDecoder(
                "/issue1298/lzw-ps-function-2-encoded.bin",
                "/issue1298/lzw-ps-function-2-decoded.txt"));
    }

    @Test
    void shouldDecodeCmapData() {
        // This sample is the much more common case where LZW data starts with the Clear Table (256) code.  LZWDecoder
        // was already decoding this sample perfectly.  Included here to verify the fix and any future changes to
        // LZWDecoder do not break this more common case.
        assertDoesNotThrow(() -> testLzwDecoder(
                "/issue1298/lzw-cmap-table-encoded.bin",
                "/issue1298/lzw-cmap-table-decoded.txt"));
    }

    private void testLzwDecoder(String encodedDataFile, String expectedDecodingFile)
            throws IOException {

        // Get LZW encoded data from test resource.  Actual data pulled from PDF streams in real-life PDF files.
        try (InputStream encodedStream = getClass().getResourceAsStream(encodedDataFile)) {
            // Read LZW encoded data taken from a PDF stream
            assertNotNull(encodedStream);
            byte[] encodedData = encodedStream.readAllBytes();

            // Use LZWDecoder directly to decode this data.  This decoder gets used in these calls:
            //    PdfStream.getBytes(true)
            //    PdfReader.getStreamBytes(PrStream)
            LZWDecoder decoder = new LZWDecoder();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            decoder.decode(encodedData, outputStream);
            byte[] decodedData = outputStream.toByteArray();

            // Read expected result and compare
            try (InputStream expectedStream = getClass().getResourceAsStream(expectedDecodingFile)) {
                assertNotNull(expectedStream);
                byte[] expectedData = expectedStream.readAllBytes();
                assertArrayEquals(expectedData, decodedData);
            }
        }
    }
}
