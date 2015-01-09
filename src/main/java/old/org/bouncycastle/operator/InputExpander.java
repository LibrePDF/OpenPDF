package old.org.bouncycastle.operator;

import java.io.InputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * General interface for an operator that is able to produce
 * an InputStream that will produce uncompressed data.
 */
public interface InputExpander
{
    /**
     * Return the algorithm identifier describing the compression
     * algorithm and parameters this expander supports.
     *
     * @return algorithm oid and parameters.
     */
    AlgorithmIdentifier getAlgorithmIdentifier();

    /**
     * Wrap the passed in input stream comIn, returning an input stream
     * that expands anything read in from comIn.
     *
     * @param comIn the compressed input data stream..
     * @return an expanding InputStream.
     */
    InputStream getInputStream(InputStream comIn);
}
