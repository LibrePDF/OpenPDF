package old.org.bouncycastle.jce.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;

/**
 * This class specifies a GOST3410-94 public key with its associated parameters.
 */

public class GOST3410PublicKeySpec
    implements KeySpec
{

    private BigInteger y;
    private BigInteger p;
    private BigInteger q;
    private BigInteger a;

    /**
     * Creates a new GOST3410PublicKeySpec with the specified parameter values.
     *
     * @param y the public key.
     * @param p the prime.
     * @param q the sub-prime.
     * @param a the base.
     */
    public GOST3410PublicKeySpec(
        BigInteger y,
        BigInteger p,
        BigInteger q,
        BigInteger a)
    {
        this.y = y;
        this.p = p;
        this.q = q;
        this.a = a;
    }

    /**
     * Returns the public key <code>y</code>.
     *
     * @return the public key <code>y</code>.
     */
    public BigInteger getY()
    {
        return this.y;
    }

    /**
     * Returns the prime <code>p</code>.
     *
     * @return the prime <code>p</code>.
     */
    public BigInteger getP()
    {
        return this.p;
    }

    /**
     * Returns the sub-prime <code>q</code>.
     *
     * @return the sub-prime <code>q</code>.
     */
    public BigInteger getQ()
    {
        return this.q;
    }

    /**
     * Returns the base <code>g</code>.
     *
     * @return the base <code>g</code>.
     */
    public BigInteger getA()
    {
        return this.a;
    }
}
