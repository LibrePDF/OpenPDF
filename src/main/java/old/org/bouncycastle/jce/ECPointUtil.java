package old.org.bouncycastle.jce;

import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

import old.org.bouncycastle.math.ec.ECCurve;

/**
 * Utility class for handling EC point decoding.
 */
public class ECPointUtil
{
    /**
     * Decode a point on this curve which has been encoded using point
     * compression (X9.62 s 4.2.1 and 4.2.2) or regular encoding.
     * 
     * @param curve
     *            The elliptic curve.
     * @param encoded
     *            The encoded point.
     * @return the decoded point.
     */
    public static ECPoint decodePoint(
       EllipticCurve curve, 
       byte[] encoded)
    {
        ECCurve c = null;
        
        if (curve.getField() instanceof ECFieldFp)
        {
            c = new ECCurve.Fp(
                    ((ECFieldFp)curve.getField()).getP(), curve.getA(), curve.getB());
        }
        else
        {
            int k[] = ((ECFieldF2m)curve.getField()).getMidTermsOfReductionPolynomial();
            
            if (k.length == 3)
            {
                c = new ECCurve.F2m(
                        ((ECFieldF2m)curve.getField()).getM(), k[2], k[1], k[0], curve.getA(), curve.getB());
            }
            else
            {
                c = new ECCurve.F2m(
                        ((ECFieldF2m)curve.getField()).getM(), k[0], curve.getA(), curve.getB());
            }
        }
        
        old.org.bouncycastle.math.ec.ECPoint p = c.decodePoint(encoded);
        
        return new ECPoint(p.getX().toBigInteger(), p.getY().toBigInteger());
    }
}
