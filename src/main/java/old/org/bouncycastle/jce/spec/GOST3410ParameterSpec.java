package old.org.bouncycastle.jce.spec;

import java.security.spec.AlgorithmParameterSpec;

import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.cryptopro.GOST3410NamedParameters;
import old.org.bouncycastle.asn1.cryptopro.GOST3410ParamSetParameters;
import old.org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import old.org.bouncycastle.jce.interfaces.GOST3410Params;

/**
 * ParameterSpec for a GOST 3410-94 key.
 */
public class GOST3410ParameterSpec
    implements AlgorithmParameterSpec, GOST3410Params
{
    private GOST3410PublicKeyParameterSetSpec keyParameters;
    private String                            keyParamSetOID;
    private String                            digestParamSetOID;
    private String                            encryptionParamSetOID;
    
    public GOST3410ParameterSpec(
        String  keyParamSetID,
        String  digestParamSetOID,
        String  encryptionParamSetOID)
    {
        GOST3410ParamSetParameters  ecP = null;
        
        try
        {
            ecP = GOST3410NamedParameters.getByOID(new DERObjectIdentifier(keyParamSetID));
        }
        catch (IllegalArgumentException e)
        {
            DERObjectIdentifier oid = GOST3410NamedParameters.getOID(keyParamSetID);
            if (oid != null)
            {
                keyParamSetID = oid.getId();
                ecP = GOST3410NamedParameters.getByOID(oid);
            }
        }
        
        if (ecP == null)
        {
            throw new IllegalArgumentException("no key parameter set for passed in name/OID.");
        }

        this.keyParameters = new GOST3410PublicKeyParameterSetSpec(
                                        ecP.getP(),
                                        ecP.getQ(),
                                        ecP.getA());
        
        this.keyParamSetOID = keyParamSetID;
        this.digestParamSetOID = digestParamSetOID;
        this.encryptionParamSetOID = encryptionParamSetOID;
    }
    
    public GOST3410ParameterSpec(
        String  keyParamSetID,
        String  digestParamSetOID)
    {
        this(keyParamSetID, digestParamSetOID, null);
    }
    
    public GOST3410ParameterSpec(
        String  keyParamSetID)
    {
        this(keyParamSetID, CryptoProObjectIdentifiers.gostR3411_94_CryptoProParamSet.getId(), null);
    }
    
    public GOST3410ParameterSpec(
        GOST3410PublicKeyParameterSetSpec spec)
    {
        this.keyParameters = spec;
        this.digestParamSetOID = CryptoProObjectIdentifiers.gostR3411_94_CryptoProParamSet.getId();
        this.encryptionParamSetOID = null;
    }
    
    public String getPublicKeyParamSetOID()
    {
        return this.keyParamSetOID;
    }

    public GOST3410PublicKeyParameterSetSpec getPublicKeyParameters()
    {
        return keyParameters;
    }
    
    public String getDigestParamSetOID()
    {
        return this.digestParamSetOID;
    }

    public String getEncryptionParamSetOID()
    {
        return this.encryptionParamSetOID;
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof GOST3410ParameterSpec)
        {
            GOST3410ParameterSpec other = (GOST3410ParameterSpec)o;
            
            return this.keyParameters.equals(other.keyParameters) 
                && this.digestParamSetOID.equals(other.digestParamSetOID)
                && (this.encryptionParamSetOID == other.encryptionParamSetOID
                    || (this.encryptionParamSetOID != null && this.encryptionParamSetOID.equals(other.encryptionParamSetOID)));
        }
        
        return false;
    }
    
    public int hashCode()
    {
        return this.keyParameters.hashCode() ^ this.digestParamSetOID.hashCode() 
                       ^ (this.encryptionParamSetOID != null ? this.encryptionParamSetOID.hashCode() : 0);
    }

    public static GOST3410ParameterSpec fromPublicKeyAlg(
        GOST3410PublicKeyAlgParameters params)
    {
        if (params.getEncryptionParamSet() != null)
        {
            return new GOST3410ParameterSpec(params.getPublicKeyParamSet().getId(), params.getDigestParamSet().getId(), params.getEncryptionParamSet().getId());
        }
        else
        {
            return new GOST3410ParameterSpec(params.getPublicKeyParamSet().getId(), params.getDigestParamSet().getId());
        }
    }
}
