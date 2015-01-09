package old.org.bouncycastle.jce.provider;

import java.math.BigInteger;
import java.io.IOException;

public interface DSAEncoder
{
    byte[] encode(BigInteger r, BigInteger s)
        throws IOException;

    BigInteger[] decode(byte[] sig)
        throws IOException;
}
