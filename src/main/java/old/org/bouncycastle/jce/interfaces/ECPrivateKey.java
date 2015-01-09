package old.org.bouncycastle.jce.interfaces;

import java.math.BigInteger;
import java.security.PrivateKey;

/**
 * interface for Elliptic Curve Private keys.
 */
public interface ECPrivateKey
    extends ECKey, PrivateKey
{
    /**
     * return the private value D.
     */
    public BigInteger getD();
}
