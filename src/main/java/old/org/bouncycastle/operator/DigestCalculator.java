package old.org.bouncycastle.operator;

import java.io.OutputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * General interface for an operator that is able to calculate a digest from
 * a stream of output.
 */
public interface DigestCalculator
{
    /**
     * Return the algorithm identifier representing the digest implemented by
     * this calculator.
     *
     * @return algorithm id and parameters.
     */
    AlgorithmIdentifier getAlgorithmIdentifier();

    /**
     * Returns a stream that will accept data for the purpose of calculating
     * a digest. Use org.bouncycastle.util.io.TeeOutputStream if you want to accumulate
     * the data on the fly as well.
     *
     * @return an OutputStream
     */
    OutputStream getOutputStream();

    /**
     * Return the digest calculated on what has been written to the calculator's output stream.
     *
     * @return a digest.
     */
    byte[] getDigest();
}
