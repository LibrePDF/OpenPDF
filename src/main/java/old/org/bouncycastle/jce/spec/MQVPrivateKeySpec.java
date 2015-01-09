package old.org.bouncycastle.jce.spec;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;

import old.org.bouncycastle.jce.interfaces.MQVPrivateKey;

/**
 * Static/ephemeral private key (pair) for use with ECMQV key agreement
 * (Optionally provides the ephemeral public key)
 */
public class MQVPrivateKeySpec
    implements KeySpec, MQVPrivateKey
{
    private PrivateKey staticPrivateKey;
    private PrivateKey ephemeralPrivateKey;
    private PublicKey ephemeralPublicKey;

    /**
     * @param staticPrivateKey the static private key.
     * @param ephemeralPrivateKey the ephemeral private key.
     */
    public MQVPrivateKeySpec(
            PrivateKey  staticPrivateKey,
            PrivateKey  ephemeralPrivateKey)
    {
        this(staticPrivateKey, ephemeralPrivateKey, null);
    }

    /**
     * @param staticPrivateKey the static private key.
     * @param ephemeralPrivateKey the ephemeral private key.
     * @param ephemeralPublicKey the ephemeral public key (may be null).
     */
    public MQVPrivateKeySpec(
        PrivateKey  staticPrivateKey,
        PrivateKey  ephemeralPrivateKey,
        PublicKey   ephemeralPublicKey)
    {
        this.staticPrivateKey = staticPrivateKey;
        this.ephemeralPrivateKey = ephemeralPrivateKey;
        this.ephemeralPublicKey = ephemeralPublicKey;
    }

    /**
     * return the static private key
     */
    public PrivateKey getStaticPrivateKey()
    {
        return staticPrivateKey;
    }

    /**
     * return the ephemeral private key
     */
    public PrivateKey getEphemeralPrivateKey()
    {
        return ephemeralPrivateKey;
    }

    /**
     * return the ephemeral public key (may be null)
     */
    public PublicKey getEphemeralPublicKey()
    {
        return ephemeralPublicKey;
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
