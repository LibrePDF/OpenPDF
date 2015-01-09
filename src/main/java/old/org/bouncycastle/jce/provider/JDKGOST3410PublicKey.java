package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import old.org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.crypto.params.GOST3410PublicKeyParameters;
import old.org.bouncycastle.jce.interfaces.GOST3410Params;
import old.org.bouncycastle.jce.interfaces.GOST3410PublicKey;
import old.org.bouncycastle.jce.spec.GOST3410ParameterSpec;
import old.org.bouncycastle.jce.spec.GOST3410PublicKeyParameterSetSpec;
import old.org.bouncycastle.jce.spec.GOST3410PublicKeySpec;

import java.io.IOException;
import java.math.BigInteger;

public class JDKGOST3410PublicKey
    implements GOST3410PublicKey
{
    private BigInteger      y;
    private GOST3410Params  gost3410Spec;

    JDKGOST3410PublicKey(
        GOST3410PublicKeySpec    spec)
    {
        this.y = spec.getY();
        this.gost3410Spec = new GOST3410ParameterSpec(new GOST3410PublicKeyParameterSetSpec(spec.getP(), spec.getQ(), spec.getA()));
    }

    JDKGOST3410PublicKey(
        GOST3410PublicKey    key)
    {
        this.y = key.getY();
        this.gost3410Spec = key.getParameters();
    }

    JDKGOST3410PublicKey(
        GOST3410PublicKeyParameters  params,
        GOST3410ParameterSpec        spec)
    {
        this.y = params.getY();
        this.gost3410Spec = spec;
    }

    JDKGOST3410PublicKey(
        BigInteger        y,
        GOST3410ParameterSpec  gost3410Spec)
    {
        this.y = y;
        this.gost3410Spec = gost3410Spec;
    }

    JDKGOST3410PublicKey(
        SubjectPublicKeyInfo    info)
    {
        GOST3410PublicKeyAlgParameters    params = new GOST3410PublicKeyAlgParameters((ASN1Sequence)info.getAlgorithmId().getParameters());
        DEROctetString                    derY;

        try
        {
            derY = (DEROctetString)info.getPublicKey();
            
            byte[]                  keyEnc = derY.getOctets();
            byte[]                  keyBytes = new byte[keyEnc.length];
            
            for (int i = 0; i != keyEnc.length; i++)
            {
                keyBytes[i] = keyEnc[keyEnc.length - 1 - i]; // was little endian
            }

            this.y = new BigInteger(1, keyBytes);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid info structure in GOST3410 public key");
        }

        this.gost3410Spec = GOST3410ParameterSpec.fromPublicKeyAlg(params);
    }

    public String getAlgorithm()
    {
        return "GOST3410";
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getEncoded()
    {
        SubjectPublicKeyInfo    info;
        byte[]                  keyEnc = this.getY().toByteArray();
        byte[]                  keyBytes;
        
        if (keyEnc[0] == 0)
        {
            keyBytes = new byte[keyEnc.length - 1];
        }
        else
        {
            keyBytes = new byte[keyEnc.length];
        }
        
        for (int i = 0; i != keyBytes.length; i++)
        {
            keyBytes[i] = keyEnc[keyEnc.length - 1 - i]; // must be little endian
        }
        
        if (gost3410Spec instanceof GOST3410ParameterSpec)
        {   
            if (gost3410Spec.getEncryptionParamSetOID() != null)
            {
                info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(CryptoProObjectIdentifiers.gostR3410_94, new GOST3410PublicKeyAlgParameters(new DERObjectIdentifier(gost3410Spec.getPublicKeyParamSetOID()), new DERObjectIdentifier(gost3410Spec.getDigestParamSetOID()), new DERObjectIdentifier(gost3410Spec.getEncryptionParamSetOID())).getDERObject()), new DEROctetString(keyBytes));
            }
            else
            {
                info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(CryptoProObjectIdentifiers.gostR3410_94, new GOST3410PublicKeyAlgParameters(new DERObjectIdentifier(gost3410Spec.getPublicKeyParamSetOID()), new DERObjectIdentifier(gost3410Spec.getDigestParamSetOID())).getDERObject()), new DEROctetString(keyBytes));
            }
        }
        else
        {
            info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(CryptoProObjectIdentifiers.gostR3410_94), new DEROctetString(keyBytes));
        }

        return info.getDEREncoded();
    }

    public GOST3410Params getParameters()
    {
        return gost3410Spec;
    }

    public BigInteger getY()
    {
        return y;
    }

    public String toString()
    {
        StringBuffer    buf = new StringBuffer();
        String          nl = System.getProperty("line.separator");

        buf.append("GOST3410 Public Key").append(nl);
        buf.append("            y: ").append(this.getY().toString(16)).append(nl);

        return buf.toString();
    }
    
    public boolean equals(Object o)
    {
        if (o instanceof JDKGOST3410PublicKey)
        {
            JDKGOST3410PublicKey other = (JDKGOST3410PublicKey)o;
            
            return this.y.equals(other.y) && this.gost3410Spec.equals(other.gost3410Spec);
        }
        
        return false;
    }
    
    public int hashCode()
    {
        return y.hashCode() ^ gost3410Spec.hashCode();
    }
}
