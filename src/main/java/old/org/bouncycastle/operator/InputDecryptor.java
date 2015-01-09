package old.org.bouncycastle.operator;

import java.io.InputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * General interface for an operator that is able to produce
 * an InputStream that will decrypt a stream of encrypted data.
 */
public interface InputDecryptor
{
    /**
     * Return the algorithm identifier describing the encryption
     * algorithm and parameters this decryptor can process.
     *
     * @return algorithm oid and parameters.
     */
    AlgorithmIdentifier getAlgorithmIdentifier();

    /**
     * Wrap the passed in input stream encIn, returning an input stream
     * that decrypts what it reads from encIn before returning it.
     *
     * @param encIn InputStream containing encrypted input.
     * @return an decrypting InputStream
     */
    InputStream getInputStream(InputStream encIn);
}
