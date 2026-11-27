package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * The PNG predictor must check /Columns, /Colors and /BitsPerComponent before it reserves memory for a row.
 */
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class PdfReaderPngPredictorTest {

    private static final int UP = 12;
    private static final byte[] TWO_ROWS = {2, 1, 2, 3, 2, 1, 1, 1};

    @Test
    void rowsAreDecoded() {
        assertThat(PdfReader.decodePredictor(TWO_ROWS, parameters(UP, 3, 1, 8))).containsExactly(1, 2, 3, 2, 3, 4);
    }

    @Test
    void subFilterUsesBytesPerPixel() {
        byte[] in = {1, 1, 2, 1, 1, 1, 1};

        assertThat(PdfReader.decodePredictor(in, parameters(11, 1, 3, 16))).containsExactly(1, 2, 1, 1, 1, 1);
        assertThat(PdfReader.decodePredictor(in, parameters(11, 3, 1, 16))).containsExactly(1, 2, 2, 3, 3, 4);
    }

    @Test
    void truncatedLastRowIsDropped() {
        byte[] in = {2, 1, 2, 3, 2, 1};

        assertThat(PdfReader.decodePredictor(in, parameters(UP, 3, 1, 8))).containsExactly(1, 2, 3);
    }

    @Test
    void inputShorterThanOneRowGivesNoOutput() {
        assertThat(PdfReader.decodePredictor(TWO_ROWS, parameters(UP, 8, 1, 8))).isEmpty();
        assertThat(PdfReader.decodePredictor(new byte[0], parameters(UP, 3, 1, 8))).isEmpty();
    }

    @Test
    void hugeRowDoesNotReserveMemory() {
        assertThat(PdfReader.decodePredictor(TWO_ROWS, parameters(UP, Integer.MAX_VALUE, 1, 8))).isEmpty();
        assertThat(PdfReader.decodePredictor(TWO_ROWS, parameters(UP, 1, Integer.MAX_VALUE, 8))).isEmpty();
        assertThat(PdfReader.decodePredictor(TWO_ROWS, parameters(UP, 1_000_000_000, 4, 16))).isEmpty();
        assertThat(PdfReader.decodePredictor(TWO_ROWS, parameters(UP, Integer.MAX_VALUE, Integer.MAX_VALUE, 16)))
                .isEmpty();
    }

    @Test
    void invalidParametersAreRejected() {
        assertInvalid(parameters(UP, 0, 1, 8));
        assertInvalid(parameters(UP, -1, 1, 8));
        assertInvalid(parameters(UP, 3, 0, 8));
        assertInvalid(parameters(UP, 3, -1, 8));
        assertInvalid(parameters(UP, 3, 1, 0));
        assertInvalid(parameters(UP, 3, 1, 3));
        assertInvalid(parameters(UP, 3, 1, -8));
        assertInvalid(parameters(UP, 3, 1, 32));
    }

    private static void assertInvalid(PdfDictionary parameters) {
        assertThatThrownBy(() -> PdfReader.decodePredictor(TWO_ROWS, parameters))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid PNG predictor parameters");
    }

    private static PdfDictionary parameters(int predictor, int columns, int colors, int bitsPerComponent) {
        PdfDictionary parameters = new PdfDictionary();
        parameters.put(PdfName.PREDICTOR, new PdfNumber(predictor));
        parameters.put(PdfName.COLUMNS, new PdfNumber(columns));
        parameters.put(PdfName.COLORS, new PdfNumber(colors));
        parameters.put(PdfName.BITSPERCOMPONENT, new PdfNumber(bitsPerComponent));
        return parameters;
    }
}
