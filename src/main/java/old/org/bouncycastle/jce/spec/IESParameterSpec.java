package old.org.bouncycastle.jce.spec;

import java.security.spec.AlgorithmParameterSpec;

/**
 * Parameter spec for an integrated encryptor, as in IEEE P1363a
 */
public class IESParameterSpec
    implements AlgorithmParameterSpec
{
    private byte[]  derivation;
    private byte[]  encoding;
    private int     macKeySize;

    public IESParameterSpec(
        byte[]  derivation,
        byte[]  encoding,
        int     macKeySize)
    {
        this.derivation = new byte[derivation.length];
        System.arraycopy(derivation, 0, this.derivation, 0, derivation.length);

        this.encoding = new byte[encoding.length];
        System.arraycopy(encoding, 0, this.encoding, 0, encoding.length);

        this.macKeySize = macKeySize;           
    }

    /**
     * return the derivation vector.
     */
    public byte[] getDerivationV()
    {
        return derivation;
    }

    /**
     * return the encoding vector.
     */
    public byte[] getEncodingV()
    {
        return encoding;
    }

    /**
     * return the key size in bits for the MAC used with the message
     */
    public int getMacKeySize()
    {
        return macKeySize;
    }
}
