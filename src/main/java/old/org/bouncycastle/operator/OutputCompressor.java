package old.org.bouncycastle.operator;

import java.io.OutputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * General interface for an operator that is able to produce
 * an OutputStream that will output compressed data.
 */
public interface OutputCompressor
{
    /**
     * Return the algorithm identifier describing the compression
     * algorithm and parameters this compressor uses.
     *
     * @return algorithm oid and parameters.
     */
    AlgorithmIdentifier getAlgorithmIdentifier();

    /**
     * Wrap the passed in output stream comOut, returning an output stream
     * that compresses anything passed in before sending on to comOut.
     *
     * @param comOut output stream for compressed output.
     * @return a compressing OutputStream
     */
    OutputStream getOutputStream(OutputStream comOut);
}
