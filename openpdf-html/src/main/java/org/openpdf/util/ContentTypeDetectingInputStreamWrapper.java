package org.openpdf.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class wraps an input stream and detects if it contains certain content using "magic numbers".
 *
 * <a href="http://en.wikipedia.org/wiki/Magic_number_(programming)">...</a>
 * <p>
 * currently only pdf detection is implemented
 *
 * @author mwyraz
 */
public class ContentTypeDetectingInputStreamWrapper extends BufferedInputStream {
    private static final byte[] MAGIC_BYTES_PDF = "%PDF".getBytes(UTF_8);
    private static final int MAX_MAGIC_BYTES = 4;
    private static final byte[] NO_DATA = new byte[0];

    private final byte[] firstBytes;

    public ContentTypeDetectingInputStreamWrapper(InputStream source) throws IOException {
        super(source);
        this.firstBytes = readFirstBytes(this, MAX_MAGIC_BYTES);
    }

    private static byte[] readFirstBytes(InputStream in, int count) throws IOException {
        in.mark(count);

        try {
            byte[] buffer = new byte[count];
            int bytesRead = in.read(buffer);
            return bytesRead >= count ?
                buffer :
                bytesRead <= 0 ? NO_DATA : Arrays.copyOf(buffer, bytesRead); // Not enough data in stream
        } finally {
            in.reset();
        }
    }

    private boolean streamStartsWithMagicBytes(byte[] bytes) {
        return Arrays.equals(firstBytes, bytes);
    }

    public boolean isPdf() {
        return streamStartsWithMagicBytes(MAGIC_BYTES_PDF);
    }
}
