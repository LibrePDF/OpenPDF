package old.org.bouncycastle.jce.provider.asymmetric.ec;

import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

import old.org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import old.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import old.org.bouncycastle.math.ec.ECCurve;

public class EC5Util
{
    public static EllipticCurve convertCurve(
        ECCurve curve, 
        byte[]  seed)
    {
        // TODO: the Sun EC implementation doesn't currently handle the seed properly
        // so at the moment it's set to null. Should probably look at making this configurable
        if (curve instanceof ECCurve.Fp)
        {
            return new EllipticCurve(new ECFieldFp(((ECCurve.Fp)curve).getQ()), curve.getA().toBigInteger(), curve.getB().toBigInteger(), null);
        }
        else
        {
            ECCurve.F2m curveF2m = (ECCurve.F2m)curve;
            int ks[];
            
            if (curveF2m.isTrinomial())
            {
                ks = new int[] { curveF2m.getK1() };
                
                return new EllipticCurve(new ECFieldF2m(curveF2m.getM(), ks), curve.getA().toBigInteger(), curve.getB().toBigInteger(), null);
            }
            else
            {
                ks = new int[] { curveF2m.getK3(), curveF2m.getK2(), curveF2m.getK1() };
                
                return new EllipticCurve(new ECFieldF2m(curveF2m.getM(), ks), curve.getA().toBigInteger(), curve.getB().toBigInteger(), null);
            } 
        }
    }

    public static ECCurve convertCurve(
        EllipticCurve ec)
    {
        ECField field = ec.getField();
        BigInteger a = ec.getA();
        BigInteger b = ec.getB();

        if (field instanceof ECFieldFp)
        {
            return new ECCurve.Fp(((ECFieldFp)field).getP(), a, b);
        }
        else
        {
            ECFieldF2m fieldF2m = (ECFieldF2m)field;
            int m = fieldF2m.getM();
            int ks[] = ECUtil.convertMidTerms(fieldF2m.getMidTermsOfReductionPolynomial());
            return new ECCurve.F2m(m, ks[0], ks[1], ks[2], a, b); 
        }
    }

    public static ECParameterSpec convertSpec(
        EllipticCurve ellipticCurve,
        old.org.bouncycastle.jce.spec.ECParameterSpec spec)
    {
        if (spec instanceof ECNamedCurveParameterSpec)
        {
            return new ECNamedCurveSpec(
                ((ECNamedCurveParameterSpec)spec).getName(),
                ellipticCurve,
                new ECPoint(
                    spec.getG().getX().toBigInteger(),
                    spec.getG().getY().toBigInteger()),
                spec.getN(),
                spec.getH());
        }
        else
        {
            return new ECParameterSpec(
                ellipticCurve,
                new ECPoint(
                    spec.getG().getX().toBigInteger(),
                    spec.getG().getY().toBigInteger()),
                spec.getN(),
                spec.getH().intValue());
        }
    }

    public static old.org.bouncycastle.jce.spec.ECParameterSpec convertSpec(
        ECParameterSpec ecSpec,
        boolean withCompression)
    {
        ECCurve curve = convertCurve(ecSpec.getCurve());

        return new old.org.bouncycastle.jce.spec.ECParameterSpec(
            curve,
            convertPoint(curve, ecSpec.getGenerator(), withCompression),
            ecSpec.getOrder(),
            BigInteger.valueOf(ecSpec.getCofactor()),
            ecSpec.getCurve().getSeed());
    }

    public static old.org.bouncycastle.math.ec.ECPoint convertPoint(
        ECParameterSpec ecSpec,
        ECPoint point,
        boolean withCompression)
    {
        return convertPoint(convertCurve(ecSpec.getCurve()), point, withCompression);
    }

    public static old.org.bouncycastle.math.ec.ECPoint convertPoint(
        ECCurve curve,
        ECPoint point,
        boolean withCompression)
    {
        return curve.createPoint(point.getAffineX(), point.getAffineY(), withCompression);
    }
}
