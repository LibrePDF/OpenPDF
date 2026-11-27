/*
 * Copyright 2026 OpenPDF
 *
 * Licensed under the LGPL 2.1 or MPL 2.0.
 */
package org.openpdf.text.pdf.codec;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNull;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfReader;

class BrotliPredictorTest {
    private static final byte[] EXPECTED = {100, 3, 101, 2, 102, 1};
    private static final byte[] PNG = HexFormat.of().parseHex("0b04800264030201ff0201ff03");
    private static final byte[] TIFF = HexFormat.of().parseHex("8b0280649f659d669b03");

    private static PdfDictionary parameters(int predictor) {
        PdfDictionary parameters = new PdfDictionary();
        parameters.put(PdfName.PREDICTOR, new PdfNumber(predictor));
        parameters.put(PdfName.COLUMNS, new PdfNumber(2));
        return parameters;
    }

    private static PRStream stream(byte[] data) {
        PRStream stream = new PRStream(null, -1);
        stream.setData(data, false);
        stream.put(PdfName.FILTER, PdfName.BROTLIDECODE);
        return stream;
    }

    @Test
    void pngPredictorIsAppliedAfterBrotli() throws Exception {
        PRStream stream = stream(PNG);
        stream.put(PdfName.DECODEPARMS, parameters(12));
        assertThat(PdfReader.getStreamBytes(stream, null)).isEqualTo(EXPECTED);
    }

    @Test
    void tiffPredictorIsAppliedAfterBrotli() throws Exception {
        PRStream stream = stream(TIFF);
        stream.put(PdfName.DECODEPARMS, parameters(2));
        assertThat(PdfReader.getStreamBytes(stream, null)).isEqualTo(EXPECTED);
    }

    @Test
    void predictorUsesMatchingEntryInFilterArray() throws Exception {
        PRStream stream = stream("0b04800264030201ff0201ff03>".getBytes(StandardCharsets.US_ASCII));
        PdfArray filters = new PdfArray();
        filters.add(PdfName.ASCIIHEXDECODE);
        filters.add(PdfName.BROTLIDECODE);
        stream.put(PdfName.FILTER, filters);
        PdfArray parameters = new PdfArray();
        parameters.add(PdfNull.PDFNULL);
        parameters.add(parameters(12));
        stream.put(PdfName.DECODEPARMS, parameters);
        assertThat(PdfReader.getStreamBytes(stream, null)).isEqualTo(EXPECTED);
    }

    @Test
    void streamWithoutPredictorIsUnchanged() throws Exception {
        PRStream stream = stream(HexFormat.of().parseHex("8b028064036502660103"));
        assertThat(PdfReader.getStreamBytes(stream, null)).isEqualTo(EXPECTED);
    }
}
