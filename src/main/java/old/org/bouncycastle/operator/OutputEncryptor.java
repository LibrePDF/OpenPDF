package old.org.bouncycastle.operator;

import java.io.OutputStream;

import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * General interface for an operator that is able to produce
 * an OutputStream that will output encrypted data.
 */
public interface OutputEncryptor
{
    /**
     * Return the algorithm identifier describing the encryption
     * algorithm and parameters this encryptor uses.
     *
     * @return algorithm oid and parameters.
     */
    AlgorithmIdentifier getAlgorithmIdentifier();

    /**
     * Wrap the passed in output stream encOut, returning an output stream
     * that encrypts anything passed in before sending on to encOut.
     *
     * @param encOut output stream for encrypted output.
     * @return an encrypting OutputStream
     */
    OutputStream getOutputStream(OutputStream encOut);

    /**
     * Return the key used for encrypting the output.
     *
     * @return the encryption key.
     */
    GenericKey getKey();
}
