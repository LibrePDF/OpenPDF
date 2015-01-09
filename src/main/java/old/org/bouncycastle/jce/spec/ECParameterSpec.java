package old.org.bouncycastle.jce.spec;

import old.org.bouncycastle.math.ec.ECCurve;
import old.org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;

/**
 * basic domain parameters for an Elliptic Curve public or private key.
 */
public class ECParameterSpec
    implements AlgorithmParameterSpec
{
    private ECCurve     curve;
    private byte[]      seed;
    private ECPoint     G;
    private BigInteger  n;
    private BigInteger  h;

    public ECParameterSpec(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n)
    {
        this.curve = curve;
        this.G = G;
        this.n = n;
        this.h = BigInteger.valueOf(1);
        this.seed = null;
    }

    public ECParameterSpec(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n,
        BigInteger  h)
    {
        this.curve = curve;
        this.G = G;
        this.n = n;
        this.h = h;
        this.seed = null;
    }

    public ECParameterSpec(
        ECCurve     curve,
        ECPoint     G,
        BigInteger  n,
        BigInteger  h,
        byte[]      seed)
    {
        this.curve = curve;
        this.G = G;
        this.n = n;
        this.h = h;
        this.seed = seed;
    }

    /**
     * return the curve along which the base point lies.
     * @return the curve
     */
    public ECCurve getCurve()
    {
        return curve;
    }

    /**
     * return the base point we are using for these domain parameters.
     * @return the base point.
     */
    public ECPoint getG()
    {
        return G;
    }

    /**
     * return the order N of G
     * @return the order
     */
    public BigInteger getN()
    {
        return n;
    }

    /**
     * return the cofactor H to the order of G.
     * @return the cofactor
     */
    public BigInteger getH()
    {
        return h;
    }

    /**
     * return the seed used to generate this curve (if available).
     * @return the random seed
     */
    public byte[] getSeed()
    {
        return seed;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof ECParameterSpec))
        {
            return false;
        }

        ECParameterSpec other = (ECParameterSpec)o;

        return this.getCurve().equals(other.getCurve()) && this.getG().equals(other.getG());
    }

    public int hashCode()
    {
        return this.getCurve().hashCode() ^ this.getG().hashCode();
    }
}
