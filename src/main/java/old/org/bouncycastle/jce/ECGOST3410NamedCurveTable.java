package old.org.bouncycastle.jce;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import old.org.bouncycastle.crypto.params.ECDomainParameters;
import old.org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

/**
 * a table of locally supported named curves.
 */
public class ECGOST3410NamedCurveTable
{
    /**
     * return a parameter spec representing the passed in named
     * curve. The routine returns null if the curve is not present.
     * 
     * @param name the name of the curve requested
     * @return a parameter spec for the curve, null if it is not available.
     */
    public static ECNamedCurveParameterSpec getParameterSpec(
        String  name)
    {
        ECDomainParameters  ecP = ECGOST3410NamedCurves.getByName(name);
        if (ecP == null)
        {
            try
            {
                ecP = ECGOST3410NamedCurves.getByOID(new DERObjectIdentifier(name));
            }
            catch (IllegalArgumentException e)
            {
                return null; // not an oid.
            }
        }
        
        if (ecP == null)
        {
            return null;
        }

        return new ECNamedCurveParameterSpec(
                                        name,
                                        ecP.getCurve(),
                                        ecP.getG(),
                                        ecP.getN(),
                                        ecP.getH(),
                                        ecP.getSeed());
    }

    /**
     * return an enumeration of the names of the available curves.
     *
     * @return an enumeration of the names of the available curves.
     */
    public static Enumeration getNames()
    {
        return ECGOST3410NamedCurves.getNames();
    }
}
