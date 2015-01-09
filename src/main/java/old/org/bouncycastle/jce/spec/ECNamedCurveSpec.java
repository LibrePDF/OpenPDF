package old.org.bouncycastle.jce.spec;

import java.math.BigInteger;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

import old.org.bouncycastle.math.ec.ECCurve;

/**
 * specification signifying that the curve parameters can also be
 * referred to by name.
 */
public class ECNamedCurveSpec
    extends java.security.spec.ECParameterSpec
{
    private String  name;

    private static EllipticCurve convertCurve(
        ECCurve  curve,
        byte[]   seed)
    {
        if (curve instanceof ECCurve.Fp)
        {
            return new EllipticCurve(new ECFieldFp(((ECCurve.Fp)curve).getQ()), curve.getA().toBigInteger(), curve.getB().toBigInteger(), seed);
        }
        else
        {
            ECCurve.F2m curveF2m = (ECCurve.F2m)curve;
            int ks[];
            
            if (curveF2m.isTrinomial())
            {
                ks = new int[] { curveF2m.getK1() };
                
                return new EllipticCurve(new ECFieldF2m(curveF2m.getM(), ks), curve.getA().toBigInteger(), curve.getB().toBigInteger(), seed);
            }
            else
            {
                ks = new int[] { curveF2m.getK3(), curveF2m.getK2(), curveF2m.getK1() };

                return new EllipticCurve(new ECFieldF2m(curveF2m.getM(), ks), curve.getA().toBigInteger(), curve.getB().toBigInteger(), seed);
            } 
        }

    }
    
    private static ECPoint convertPoint(
        old.org.bouncycastle.math.ec.ECPoint  g)
    {
        return new ECPoint(g.getX().toBigInteger(), g.getY().toBigInteger());
    }
    
    public ECNamedCurveSpec(
        String                              name,
        ECCurve                             curve,
        old.org.bouncycastle.math.ec.ECPoint    g,
        BigInteger                          n)
    {
        super(convertCurve(curve, null), convertPoint(g), n, 1);

        this.name = name;
    }

    public ECNamedCurveSpec(
        String          name,
        EllipticCurve   curve,
        ECPoint         g,
        BigInteger      n)
    {
        super(curve, g, n, 1);

        this.name = name;
    }
    
    public ECNamedCurveSpec(
        String                              name,
        ECCurve                             curve,
        old.org.bouncycastle.math.ec.ECPoint    g,
        BigInteger                          n,
        BigInteger                          h)
    {
        super(convertCurve(curve, null), convertPoint(g), n, h.intValue());

        this.name = name;
    }

    public ECNamedCurveSpec(
        String          name,
        EllipticCurve   curve,
        ECPoint         g,
        BigInteger      n,
        BigInteger      h)
    {
        super(curve, g, n, h.intValue());

        this.name = name;
    }
    
    public ECNamedCurveSpec(
        String                              name,
        ECCurve                             curve,
        old.org.bouncycastle.math.ec.ECPoint    g,
        BigInteger                          n,
        BigInteger                          h,
        byte[]                              seed)
    {
        super(convertCurve(curve, seed), convertPoint(g), n, h.intValue());
        
        this.name = name;
    }

    /**
     * return the name of the curve the EC domain parameters belong to.
     */
    public String getName()
    {
        return name;
    }
}
