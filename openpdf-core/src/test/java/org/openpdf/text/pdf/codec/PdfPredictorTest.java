/*
 * Copyright 2026 OpenPDF
 *
 * Licensed under the LGPL 2.1 or MPL 2.0.
 */
package org.openpdf.text.pdf.codec;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HexFormat;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfReader;

class PdfPredictorTest {
    static Stream<Arguments> cases() {
        return Stream.of(
                Arguments.of(2, 1, 1, 5, "e870", "b058"),
                Arguments.of(2, 2, 1, 5, "db00", "c940"),
                Arguments.of(2, 4, 1, 5, "f3ffd0", "f210d0"),
                Arguments.of(2, 8, 1, 2, "649f659d669b", "640365026601"),
                Arguments.of(2, 16, 1, 2, "fffa000a80008000", "fffa000480000000"),
                Arguments.of(2, 8, 3, 2, "fa1e640a01f6", "fa1e64041f5a"),
                Arguments.of(11, 1, 1, 8, "0196", "96"),
                Arguments.of(13, 8, 1, 3, "03c8ce53039c6ac1", "c8326c008338"));
    }

    @ParameterizedTest
    @MethodSource("cases")
    void reconstructsSamples(int predictor, int bits, int colors, int columns, String encoded, String expected) {
        PdfDictionary parameters = new PdfDictionary();
        parameters.put(PdfName.PREDICTOR, new PdfNumber(predictor));
        parameters.put(PdfName.BITSPERCOMPONENT, new PdfNumber(bits));
        parameters.put(PdfName.COLORS, new PdfNumber(colors));
        parameters.put(PdfName.COLUMNS, new PdfNumber(columns));
        assertThat(PdfReader.decodePredictor(HexFormat.of().parseHex(encoded), parameters))
                .isEqualTo(HexFormat.of().parseHex(expected));
    }
}
