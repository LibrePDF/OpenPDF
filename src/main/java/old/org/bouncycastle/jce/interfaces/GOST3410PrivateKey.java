package old.org.bouncycastle.jce.interfaces;

import java.math.BigInteger;

public interface GOST3410PrivateKey extends GOST3410Key, java.security.PrivateKey
{

    public BigInteger getX();
}
