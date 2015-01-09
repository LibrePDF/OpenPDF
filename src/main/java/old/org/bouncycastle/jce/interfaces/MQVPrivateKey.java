package old.org.bouncycastle.jce.interfaces;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Static/ephemeral private key (pair) for use with ECMQV key agreement
 * (Optionally provides the ephemeral public key)
 */
public interface MQVPrivateKey
    extends PrivateKey
{
    /**
     * return the static private key.
     */
    PrivateKey getStaticPrivateKey();

    /**
     * return the ephemeral private key.
     */
    PrivateKey getEphemeralPrivateKey();

    /**
     * return the ephemeral public key (may be null).
     */
    PublicKey getEphemeralPublicKey();
}
