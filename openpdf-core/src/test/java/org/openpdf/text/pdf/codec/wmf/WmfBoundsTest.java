package org.openpdf.text.pdf.codec.wmf;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Image;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfWriter;

/**
 * Counts, indexes and sizes that are read from a Windows Metafile must be checked against the data of the image.
 */
class WmfBoundsTest {

    private static final int HEADER_WORDS = 3;

    @Test
    void validImageIsDrawn() throws Exception {
        byte[] wmf = wmf(1000,
                record(MetaDo.META_CREATEPENINDIRECT, 0, 10, 0, 0, 0),
                record(MetaDo.META_SELECTOBJECT, 0),
                record(MetaDo.META_POLYGON, 3, 10, 10, 500, 10, 500, 500),
                record(MetaDo.META_POLYLINE, 2, 20, 20, 600, 600),
                record(MetaDo.META_POLYPOLYGON, 2, 3, 3, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6),
                record(MetaDo.META_DELETEOBJECT, 0),
                record(0));

        String content = draw(wmf);

        // 2 + 1 + 2 * 2 lines between the points and one closing line for each of the 3 polygons
        assertThat(count(content, " l\n")).isEqualTo(10);
        assertThat(count(content, " m\n")).isEqualTo(4);
        assertThat(toPdf(wmf)).isNotEmpty();
    }

    @Test
    void imageWithoutEndRecordIsDrawn() throws Exception {
        byte[] wmf = wmf(1000, record(MetaDo.META_POLYLINE, 2, 20, 20, 600, 600));

        assertThat(count(draw(wmf), " l\n")).isEqualTo(1);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void polygonWithMorePointsThanTheRecordIsRejected() {
        byte[] wmf = wmf(1000, record(MetaDo.META_POLYGON, 0xFFFF, 10, 10, 500, 10), record(0));

        assertThatThrownBy(() -> draw(wmf)).isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> toPdf(wmf)).isInstanceOf(DocumentException.class);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void polylineWithMorePointsThanTheRecordIsRejected() {
        byte[] wmf = wmf(1000, record(MetaDo.META_POLYLINE, 3, 10, 10, 500, 10), record(0));

        assertThatThrownBy(() -> draw(wmf)).isInstanceOf(DocumentException.class);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void polyPolygonWithMorePolygonsThanTheRecordIsRejected() {
        byte[] wmf = wmf(1000, record(MetaDo.META_POLYPOLYGON, 0xFFFF, 3, 3), record(0));

        assertThatThrownBy(() -> draw(wmf)).isInstanceOf(DocumentException.class);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void polyPolygonWithMorePointsThanTheRecordIsRejected() {
        byte[] wmf = wmf(1000,
                record(MetaDo.META_POLYPOLYGON, 2, 0xFFFF, 0xFFFF, 1, 1, 2, 2, 3, 3, 4, 4),
                record(0));

        assertThatThrownBy(() -> draw(wmf)).isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> toPdf(wmf)).isInstanceOf(DocumentException.class);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void pointCountIsNotTrustedWhenRecordSizeIsWrong() {
        // the record declares the largest size, but the image ends after two points
        ByteArrayOutputStream os = header(1000);
        writeDWord(os, Integer.MAX_VALUE);
        writeWord(os, MetaDo.META_POLYGON);
        writeWord(os, 0xFFFF);
        for (int k = 0; k < 4; ++k) {
            writeWord(os, 10);
        }

        assertThatThrownBy(() -> draw(os.toByteArray())).isInstanceOf(IOException.class);
    }

    @Test
    void objectIndexOutsideTheObjectTableIsIgnored() throws Exception {
        byte[] wmf = wmf(1000,
                record(MetaDo.META_SELECTOBJECT, 0),
                record(MetaDo.META_SELECTOBJECT, 0x7FFF),
                record(MetaDo.META_DELETEOBJECT, 5),
                record(MetaDo.META_DELETEOBJECT, 0xFFFF),
                record(MetaDo.META_CREATEPENINDIRECT, 0, 10, 0, 0, 0),
                record(MetaDo.META_SELECTOBJECT, 1),
                record(MetaDo.META_SELECTOBJECT, 0),
                record(MetaDo.META_POLYLINE, 2, 20, 20, 600, 600),
                record(0));

        assertThat(count(draw(wmf), " l\n")).isEqualTo(1);
        assertThat(toPdf(wmf)).isNotEmpty();
    }

    @Test
    void objectIndexIsCheckedByMetaState() {
        MetaState state = new MetaState();
        PdfContentByte cb = new PdfContentByte(null);

        assertThatCode(() -> {
            state.selectMetaObject(0, cb);
            state.selectMetaObject(-1, cb);
            state.deleteMetaObject(0);
            state.deleteMetaObject(-1);
            state.addMetaObject(new MetaPen());
            state.selectMetaObject(1, cb);
            state.deleteMetaObject(1);
            state.deleteMetaObject(0);
            state.selectMetaObject(0, cb);
        }).doesNotThrowAnyException();
    }

    @Test
    void zeroUnitsPerInchIsRejected() {
        byte[] wmf = wmf(0, record(MetaDo.META_POLYLINE, 2, 20, 20, 600, 600), record(0));

        assertThatThrownBy(() -> draw(wmf)).isInstanceOf(DocumentException.class);
        assertThatThrownBy(() -> Image.getInstance(wmf)).hasMessageContaining("not a valid placeable");
    }

    @Test
    void imageEndingInsideRecordIsRejected() {
        ByteArrayOutputStream os = header(1000);
        writeDWord(os, 7);
        writeWord(os, MetaDo.META_RECTANGLE);
        writeWord(os, 10);

        assertThatThrownBy(() -> draw(os.toByteArray())).isInstanceOf(EOFException.class);
    }

    @Test
    void imageEndingInsideHeaderIsRejected() {
        byte[] wmf = new byte[10];
        System.arraycopy(header(1000).toByteArray(), 0, wmf, 0, wmf.length);

        assertThatThrownBy(() -> draw(wmf)).isInstanceOf(EOFException.class);
        assertThatThrownBy(() -> Image.getInstance(wmf)).hasMessageContaining("Unexpected end");
    }

    @Test
    void readsFailAtEndOfData() throws IOException {
        InputMeta in = new InputMeta(new ByteArrayInputStream(new byte[]{1, 2, 3, 4, 5, 6, 7}));

        assertThat(in.readInt()).isEqualTo(0x04030201);
        assertThat(in.readWord()).isEqualTo(0x0605);
        assertThatThrownBy(in::readWord).isInstanceOf(EOFException.class);
        assertThatThrownBy(in::readShort).isInstanceOf(EOFException.class);
        assertThatThrownBy(in::readInt).isInstanceOf(EOFException.class);
        assertThatThrownBy(in::readByte).isInstanceOf(EOFException.class);
        assertThatThrownBy(in::readColor).isInstanceOf(EOFException.class);
    }

    private static String draw(byte[] wmf) throws IOException, DocumentException {
        PdfContentByte cb = new PdfContentByte(null);
        new MetaDo(new ByteArrayInputStream(wmf), cb).readAll();
        return cb.toString();
    }

    private static byte[] toPdf(byte[] wmf) throws IOException, DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(Image.getInstance(wmf));
        document.close();
        return out.toByteArray();
    }

    private static int count(String text, String part) {
        int count = 0;
        for (int k = text.indexOf(part); k >= 0; k = text.indexOf(part, k + part.length())) {
            ++count;
        }
        return count;
    }

    /**
     * Builds a placeable Windows Metafile of 1000 by 1000 units from records.
     */
    private static byte[] wmf(int inch, byte[]... records) {
        ByteArrayOutputStream os = header(inch);
        for (byte[] record : records) {
            os.writeBytes(record);
        }
        return os.toByteArray();
    }

    private static ByteArrayOutputStream header(int inch) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // placeable header
        writeDWord(os, 0x9AC6CDD7);
        writeWord(os, 0);
        writeWord(os, 0);
        writeWord(os, 0);
        writeWord(os, 1000);
        writeWord(os, 1000);
        writeWord(os, inch);
        writeDWord(os, 0);
        writeWord(os, 0);
        // metafile header
        os.writeBytes(new byte[18]);
        return os;
    }

    /**
     * Builds a record with the right size for its parameters.
     */
    private static byte[] record(int function, int... words) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writeDWord(os, HEADER_WORDS + words.length);
        writeWord(os, function);
        for (int word : words) {
            writeWord(os, word);
        }
        return os.toByteArray();
    }

    private static void writeWord(ByteArrayOutputStream os, int value) {
        os.write(value & 0xff);
        os.write((value >>> 8) & 0xff);
    }

    private static void writeDWord(ByteArrayOutputStream os, int value) {
        writeWord(os, value & 0xffff);
        writeWord(os, (value >>> 16) & 0xffff);
    }
}
