package old.org.bouncycastle.jce.spec;

import java.math.BigInteger;
import java.security.spec.KeySpec;

/**
 * This class specifies a GOST3410-94 private key with its associated parameters.
 */

public class GOST3410PrivateKeySpec
    implements KeySpec
{
    private BigInteger x;
    private BigInteger p;
    private BigInteger q;
    private BigInteger a;

    /**
     * Creates a new GOST3410PrivateKeySpec with the specified parameter values.
     *
     * @param x the private key.
     * @param p the prime.
     * @param q the sub-prime.
     * @param a the base.
     */
    public GOST3410PrivateKeySpec(BigInteger x, BigInteger p, BigInteger q,
         BigInteger a)
    {
        this.x = x;
        this.p = p;
        this.q = q;
        this.a = a;
    }

    /**
     * Returns the private key <code>x</code>.
     * @return the private key <code>x</code>.
     */
    public BigInteger getX()
    {
        return this.x;
    }

    /**
     * Returns the prime <code>p</code>.
     * @return the prime <code>p</code>.
     */
    public BigInteger getP()
    {
        return this.p;
    }

    /**
     * Returns the sub-prime <code>q</code>.
     * @return the sub-prime <code>q</code>.
     */
    public BigInteger getQ()
    {
        return this.q;
    }

    /**
     * Returns the base <code>a</code>.
     * @return the base <code>a</code>.
     */
    public BigInteger getA()
    {
        return this.a;
    }
}
