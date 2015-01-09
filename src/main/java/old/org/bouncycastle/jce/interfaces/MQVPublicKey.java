package old.org.bouncycastle.jce.interfaces;

import java.security.PublicKey;

/**
 * Static/ephemeral public key pair for use with ECMQV key agreement
 */
public interface MQVPublicKey
    extends PublicKey
{
    /**
     * return the static public key.
     */
    PublicKey getStaticKey();

    /**
     * return the ephemeral public key.
     */
    PublicKey getEphemeralKey();
}
