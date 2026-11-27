package org.openpdf.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class Jpeg2000Test {

    private static final int BOX_UNKNOWN = 0x66726565;

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void fileEndingAfterFileTypeBoxIsRejected() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        writeSignatureAndFileType(out);
        byte[] truncated = bytes.toByteArray();
        assertThat(truncated).hasSize(32);

        assertThatThrownBy(() -> Image.getInstance(truncated))
                .isInstanceOf(ExceptionConverter.class)
                .hasMessageContaining("Unexpected end");
        assertThatThrownBy(() -> new Jpeg2000(truncated)).isInstanceOf(EOFException.class);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void fileEndingInsideBoxIsRejected() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        writeSignatureAndFileType(out);
        out.writeInt(1000);
        out.writeInt(BOX_UNKNOWN);
        out.write(new byte[10]);

        assertThatThrownBy(() -> new Jpeg2000(bytes.toByteArray())).isInstanceOf(EOFException.class);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void boxShorterThanItsHeaderIsRejected() throws IOException {
        for (int length : new int[]{2, 7, -1, Integer.MIN_VALUE}) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            writeSignatureAndFileType(out);
            out.writeInt(length);
            out.writeInt(BOX_UNKNOWN);
            writeHeaderBox(out, 30, 20);

            assertThatThrownBy(() -> new Jpeg2000(bytes.toByteArray()))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("box size");
        }
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void truncatedCodestreamIsRejected() {
        byte[] codestream = {(byte) 0xff, 0x4f, (byte) 0xff, 0x51, 0, 0x29, 0, 0, 0, 0, 0};

        assertThatThrownBy(() -> new Jpeg2000(codestream)).isInstanceOf(EOFException.class);
    }

    @Test
    void validFileIsRead() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        writeSignatureAndFileType(out);
        writeHeaderBox(out, 30, 20);

        Image image = Image.getInstance(bytes.toByteArray());

        assertThat(image).isInstanceOf(Jpeg2000.class);
        assertThat(image.getWidth()).isEqualTo(30f);
        assertThat(image.getHeight()).isEqualTo(20f);
    }

    @Test
    void boxesBeforeHeaderBoxAreSkipped() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        writeSignatureAndFileType(out);
        out.writeInt(8);
        out.writeInt(BOX_UNKNOWN);
        out.writeInt(12);
        out.writeInt(BOX_UNKNOWN);
        out.writeInt(0);
        writeHeaderBox(out, 30, 20);

        Image image = new Jpeg2000(bytes.toByteArray());

        assertThat(image.getWidth()).isEqualTo(30f);
        assertThat(image.getHeight()).isEqualTo(20f);
    }

    @Test
    void validCodestreamIsRead() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        // SOC and SIZ markers, segment length and capabilities
        out.writeInt(0xff4fff51);
        out.writeShort(41);
        out.writeShort(0);
        // image size and image offset
        out.writeInt(30);
        out.writeInt(20);
        out.writeInt(0);
        out.writeInt(0);
        // tile size and tile offset
        out.writeInt(30);
        out.writeInt(20);
        out.writeInt(0);
        out.writeInt(0);
        // number of components
        out.writeShort(3);

        Image image = Image.getInstance(bytes.toByteArray());

        assertThat(image.getWidth()).isEqualTo(30f);
        assertThat(image.getHeight()).isEqualTo(20f);
    }

    private static void writeSignatureAndFileType(DataOutputStream out) throws IOException {
        out.writeInt(12);
        out.writeInt(Jpeg2000.JP2_JP);
        out.writeInt(0x0d0a870a);
        out.writeInt(20);
        out.writeInt(Jpeg2000.JP2_FTYP);
        out.writeInt(Jpeg2000.JP2_JP2);
        out.writeInt(0);
        out.writeInt(Jpeg2000.JP2_JP2);
    }

    private static void writeHeaderBox(DataOutputStream out, int width, int height) throws IOException {
        out.writeInt(30);
        out.writeInt(Jpeg2000.JP2_JP2H);
        out.writeInt(22);
        out.writeInt(Jpeg2000.JP2_IHDR);
        out.writeInt(height);
        out.writeInt(width);
        out.writeShort(3);
        out.write(new byte[]{7, 7, 0, 0});
    }
}
