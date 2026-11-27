package org.openpdf.renderer.decode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.zip.DeflaterOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openpdf.renderer.PDFObject;
import org.openpdf.renderer.PDFParseException;

/**
 * Tests for the parameters of {@link Predictor} and for data that does not match them.
 */
@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class PredictorTest {

    private static final int TIFF = 2;
    private static final int PNG_UP = 12;

    private static PDFObject params(int predictor, int columns, int colors, int bits) {
        HashMap<String, PDFObject> dictionary = new HashMap<>();
        dictionary.put("Predictor", number(predictor));
        dictionary.put("Columns", number(columns));
        dictionary.put("Colors", number(colors));
        dictionary.put("BitsPerComponent", number(bits));
        return new PDFObject(null, PDFObject.DICTIONARY, dictionary);
    }

    private static PDFObject number(int value) {
        return new PDFObject(null, PDFObject.NUMBER, Double.valueOf(value));
    }

    private static byte[] toArray(ByteBuffer buffer) {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return bytes;
    }

    private static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DeflaterOutputStream zip = new DeflaterOutputStream(out)) {
            zip.write(data);
        }
        return out.toByteArray();
    }

    @Test
    void validParametersAreAccepted() throws IOException {
        Predictor predictor = Predictor.getPredictor(params(TIFF, 30, 3, 16));

        assertThat(predictor).isInstanceOf(TIFFPredictor.class);
        assertThat(predictor.getColumns()).isEqualTo(30);
        assertThat(predictor.getColors()).isEqualTo(3);
        assertThat(predictor.getBitsPerComponent()).isEqualTo(16);
        assertThat(predictor.getBytesPerRow()).isEqualTo(180);
        assertThat(Predictor.getPredictor(params(PNG_UP, 5, 1, 1)).getBytesPerRow()).isEqualTo(1);
    }

    @Test
    void defaultParametersAreAccepted() throws IOException {
        HashMap<String, PDFObject> dictionary = new HashMap<>();
        dictionary.put("Predictor", number(PNG_UP));

        Predictor predictor = Predictor.getPredictor(new PDFObject(null, PDFObject.DICTIONARY, dictionary));

        assertThat(predictor).isInstanceOf(PNGPredictor.class);
        assertThat(predictor.getBytesPerRow()).isEqualTo(1);
    }

    @ParameterizedTest
    @CsvSource({
            "2, 0, 1, 8",
            "2, 1, 0, 8",
            "2, -1, 1, 8",
            "2, 1, -1, 8",
            "2, 1, 1, 0",
            "2, 1, 1, 3",
            "2, 1, 1, 32",
            "2, 2147483647, 2147483647, 8",
            "2, 2147483647, 1, 16",
            "12, 0, 1, 8",
            "12, 1, 0, 8",
            "12, 1, 1, 0",
            "12, 2147483647, 4, 8",
    })
    void invalidParametersAreRejected(int predictor, int columns, int colors, int bits) {
        assertThatThrownBy(() -> Predictor.getPredictor(params(predictor, columns, colors, bits)))
                .isInstanceOf(PDFParseException.class);
    }

    @Test
    void streamWithoutColumnsIsRejected() throws IOException {
        ByteBuffer data = ByteBuffer.wrap(deflate(new byte[]{1, 2, 3, 4}));

        assertThatThrownBy(() -> FlateDecode.decode(null, data, params(TIFF, 0, 1, 8)))
                .isInstanceOf(PDFParseException.class);
    }

    @Test
    void streamWithoutColorsIsRejected() throws IOException {
        ByteBuffer data = ByteBuffer.wrap(deflate(new byte[]{1, 2, 3, 4}));

        assertThatThrownBy(() -> FlateDecode.decode(null, data, params(TIFF, 4, 0, 8)))
                .isInstanceOf(PDFParseException.class);
    }

    @Test
    void invalidParametersSetOnThePredictorAreRejected() {
        TIFFPredictor tiff = new TIFFPredictor();
        tiff.setColumns(0);
        PNGPredictor png = new PNGPredictor();
        png.setBitsPerComponent(0);

        assertThatThrownBy(() -> tiff.unpredict(ByteBuffer.wrap(new byte[]{1, 2, 3})))
                .isInstanceOf(PDFParseException.class);
        assertThatThrownBy(() -> png.unpredict(ByteBuffer.wrap(new byte[]{1, 2, 3})))
                .isInstanceOf(PDFParseException.class);
    }

    @Test
    void tiffRowsAreDecoded() throws IOException {
        Predictor predictor = Predictor.getPredictor(params(TIFF, 3, 1, 8));

        ByteBuffer out = predictor.unpredict(ByteBuffer.wrap(new byte[]{1, 1, 1, 5, 1, 1}));

        assertThat(toArray(out)).containsExactly(1, 2, 3, 5, 6, 7);
    }

    @Test
    void tiffDataAfterTheLastCompleteRowIsIgnored() throws IOException {
        Predictor predictor = Predictor.getPredictor(params(TIFF, 3, 1, 8));

        ByteBuffer out = predictor.unpredict(ByteBuffer.wrap(new byte[]{1, 1, 1, 5, 1}));

        assertThat(toArray(out)).containsExactly(1, 2, 3);
    }

    @Test
    void tiffRowLongerThanTheDataGivesNoRows() throws IOException {
        Predictor predictor = Predictor.getPredictor(params(TIFF, 2000000000, 1, 8));

        ByteBuffer out = predictor.unpredict(ByteBuffer.wrap(new byte[]{1, 2, 3}));

        assertThat(out.remaining()).isZero();
    }

    @Test
    void pngRowsAreDecoded() throws IOException {
        Predictor predictor = Predictor.getPredictor(params(PNG_UP, 3, 1, 8));

        ByteBuffer out = predictor.unpredict(ByteBuffer.wrap(new byte[]{2, 1, 2, 3, 2, 1, 1, 1, 0, 9}));

        assertThat(toArray(out)).containsExactly(1, 2, 3, 2, 3, 4);
    }

    @Test
    void pngRowLongerThanTheDataGivesNoRows() throws IOException {
        Predictor predictor = Predictor.getPredictor(params(PNG_UP, 300000000, 1, 8));

        ByteBuffer out = predictor.unpredict(ByteBuffer.wrap(new byte[]{2, 1, 2, 3}));

        assertThat(out.remaining()).isZero();
    }
}
