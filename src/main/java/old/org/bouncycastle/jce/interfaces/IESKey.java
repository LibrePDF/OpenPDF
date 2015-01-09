package old.org.bouncycastle.jce.interfaces;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * key pair for use with an integrated encryptor
 */
public interface IESKey
    extends Key
{
    /**
     * return the intended recipient's/sender's public key.
     */
    public PublicKey getPublic();

    /**
     * return the local private key.
     */
    public PrivateKey getPrivate();
}
