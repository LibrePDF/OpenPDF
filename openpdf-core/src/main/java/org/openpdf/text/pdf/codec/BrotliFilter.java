/*
 * Copyright 2026 OpenPDF
 *
 * Licensed under the LGPL 2.1 or MPL 2.0.
 */
package org.openpdf.text.pdf.codec;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.DecoderJNI;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import com.aayushatharva.brotli4j.encoder.BrotliOutputStream;
import com.aayushatharva.brotli4j.encoder.Encoder;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Brotli compression / decompression helper used to support PDF streams that use the
 * {@code /BrotliDecode} filter. Brotli is being standardised as a stream filter for
 * PDF 2.0 (ISO 32000-2) through ISO/TS 32001.
 * <p>
 * Backed by <a href="https://github.com/hyperxpro/Brotli4j">brotli4j</a>. The native
 * library is loaded lazily on first use; consumers that never touch Brotli pay no cost.
 */
public final class BrotliFilter {

    private BrotliFilter() {
    }

    private static void ensureAvailable() throws IOException {
        try {
            Brotli4jLoader.ensureAvailability();
        } catch (Exception e) {
            throw new IOException("Brotli native library could not be loaded. "
                    + "Add a brotli4j native dependency for your platform.", e);
        }
    }

    /**
     * Compresses the given data using Brotli (default quality).
     *
     * @param in the data to compress
     * @return the Brotli-compressed bytes
     * @throws IOException on compression error
     */
    public static byte[] encode(byte[] in) throws IOException {
        ensureAvailable();
        return Encoder.compress(in);
    }

    /**
     * Returns a new streaming Brotli encoder that writes its compressed output to
     * {@code out}. Closing the returned stream flushes and finishes the Brotli frame.
     *
     * @param out the destination for the compressed bytes
     * @return an {@link OutputStream} that Brotli-compresses everything written to it
     * @throws IOException if the encoder cannot be created
     */
    public static OutputStream encoder(OutputStream out) throws IOException {
        ensureAvailable();
        return new BrotliOutputStream(out);
    }

    /**
     * Decompresses Brotli-encoded data.
     *
     * @param in the compressed data
     * @return the decoded bytes
     * @throws IOException if the data cannot be decoded
     */
    public static byte[] decode(byte[] in) throws IOException {
        ensureAvailable();
        DirectDecompress result = DirectDecompress.decompress(in);
        if (result.getResultStatus() != DecoderJNI.Status.DONE) {
            throw new IOException("Brotli decoding failed: " + result.getResultStatus());
        }
        return result.getDecompressedData();
    }
}

