package org.openpdf.text.pdf;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Rows are filtered here as the PNG specification describes (section 9, "Filtering"), then given to
 * {@link PdfReader#decodePredictor(byte[], PdfObject)}, which must return the rows as they were.
 */
class PdfReaderPredictorTest {

    private static final int COLORS = 3;
    private static final int COLUMNS = 7;
    private static final int ROWS = 5;
    private static final int BYTES_PER_PIXEL = COLORS;
    private static final int BYTES_PER_ROW = COLORS * COLUMNS;

    private static byte[] image() {
        byte[] image = new byte[BYTES_PER_ROW * ROWS];
        // A fixed pattern that runs through byte values on both sides of 128, which a signed byte gets wrong.
        for (int i = 0; i < image.length; i++) {
            image[i] = (byte) (i * 37 + 11);
        }
        image[0] = (byte) 200;
        image[1] = (byte) 150;
        image[2] = (byte) 255;
        return image;
    }

    private static int paeth(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) {
            return a;
        }
        return pb <= pc ? b : c;
    }

    private static byte[] filter(byte[] image, int filter) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int row = 0; row < ROWS; row++) {
            out.write(filter);
            for (int i = 0; i < BYTES_PER_ROW; i++) {
                int at = row * BYTES_PER_ROW + i;
                int left = i >= BYTES_PER_PIXEL ? image[at - BYTES_PER_PIXEL] & 0xff : 0;
                int up = row > 0 ? image[at - BYTES_PER_ROW] & 0xff : 0;
                int upLeft = row > 0 && i >= BYTES_PER_PIXEL
                        ? image[at - BYTES_PER_ROW - BYTES_PER_PIXEL] & 0xff : 0;
                int predicted = switch (filter) {
                    case 1 -> left;
                    case 2 -> up;
                    case 3 -> (left + up) / 2;
                    case 4 -> paeth(left, up, upLeft);
                    default -> 0;
                };
                out.write((image[at] & 0xff) - predicted);
            }
        }
        return out.toByteArray();
    }

    private static PdfDictionary decodeParms() {
        PdfDictionary parms = new PdfDictionary();
        parms.put(PdfName.PREDICTOR, new PdfNumber(15));
        parms.put(PdfName.COLORS, new PdfNumber(COLORS));
        parms.put(PdfName.COLUMNS, new PdfNumber(COLUMNS));
        parms.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
        return parms;
    }

    @ParameterizedTest(name = "PNG filter type {0}")
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void decodesEachPngFilter(int filter) {
        byte[] image = image();

        byte[] decoded = PdfReader.decodePredictor(filter(image, filter), decodeParms());

        assertThat(decoded).isEqualTo(image);
    }
}
