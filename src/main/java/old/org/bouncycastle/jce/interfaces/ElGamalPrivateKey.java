package old.org.bouncycastle.jce.interfaces;

import java.math.BigInteger;
import java.security.PrivateKey;

public interface ElGamalPrivateKey
    extends ElGamalKey, PrivateKey
{
    public BigInteger getX();
}
