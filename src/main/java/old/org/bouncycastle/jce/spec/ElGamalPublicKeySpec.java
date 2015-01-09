package old.org.bouncycastle.jce.spec;

import java.math.BigInteger;

/**
 * This class specifies an ElGamal public key with its associated parameters.
 *
 * @see ElGamalPrivateKeySpec
 */
public class ElGamalPublicKeySpec
    extends ElGamalKeySpec
{
    private BigInteger  y;

    public ElGamalPublicKeySpec(
        BigInteger              y,
        ElGamalParameterSpec    spec)
    {
        super(spec);

        this.y = y;
    }

    /**
     * Returns the public value <code>y</code>.
     *
     * @return the public value <code>y</code>
     */
    public BigInteger getY()
    {
        return y;
    }
}
