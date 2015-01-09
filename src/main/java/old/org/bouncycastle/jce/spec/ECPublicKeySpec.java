package old.org.bouncycastle.jce.spec;

import old.org.bouncycastle.math.ec.ECPoint;

/**
 * Elliptic Curve public key specification
 */
public class ECPublicKeySpec
    extends ECKeySpec
{
    private ECPoint    q;

    /**
     * base constructor
     *
     * @param q the public point on the curve.
     * @param spec the domain parameters for the curve.
     */
    public ECPublicKeySpec(
        ECPoint         q,
        ECParameterSpec spec)
    {
        super(spec);

        this.q = q;
    }

    /**
     * return the public point q
     */
    public ECPoint getQ()
    {
        return q;
    }
}
