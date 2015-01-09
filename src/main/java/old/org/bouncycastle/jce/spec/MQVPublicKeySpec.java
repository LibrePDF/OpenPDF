package old.org.bouncycastle.jce.spec;

import java.security.PublicKey;
import java.security.spec.KeySpec;

import old.org.bouncycastle.jce.interfaces.MQVPublicKey;

/**
 * Static/ephemeral public key pair for use with ECMQV key agreement
 */
public class MQVPublicKeySpec
    implements KeySpec, MQVPublicKey
{
    private PublicKey staticKey;
    private PublicKey ephemeralKey;

    /**
     * @param staticKey the static public key.
     * @param ephemeralKey the ephemeral public key.
     */
    public MQVPublicKeySpec(
        PublicKey staticKey,
        PublicKey ephemeralKey)
    {
        this.staticKey = staticKey;
        this.ephemeralKey = ephemeralKey;
    }

    /**
     * return the static public key
     */
    public PublicKey getStaticKey()
    {
        return staticKey;
    }
    
    /**
     * return the ephemeral public key
     */
    public PublicKey getEphemeralKey()
    {
        return ephemeralKey;
    }

    /**
     * return "ECMQV"
     */
    public String getAlgorithm()
    {
        return "ECMQV";
    }

    /**
     * return null
     */
    public String getFormat()
    {
        return null;
    }

    /**
     * returns null
     */
    public byte[] getEncoded()
    {
        return null;
    }
}
