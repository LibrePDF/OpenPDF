package old.org.bouncycastle.jce.interfaces;

import java.security.PublicKey;
import java.math.BigInteger;

public interface GOST3410PublicKey extends GOST3410Key, PublicKey
{

    public BigInteger getY();
}
