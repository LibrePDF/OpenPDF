package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.crypto.params.RSAKeyParameters;

import java.io.IOException;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

public class JCERSAPublicKey
    implements RSAPublicKey
{
    static final long serialVersionUID = 2675817738516720772L;
    
    private BigInteger modulus;
    private BigInteger publicExponent;

    JCERSAPublicKey(
        RSAKeyParameters key)
    {
        this.modulus = key.getModulus();
        this.publicExponent = key.getExponent();
    }

    JCERSAPublicKey(
        RSAPublicKeySpec spec)
    {
        this.modulus = spec.getModulus();
        this.publicExponent = spec.getPublicExponent();
    }

    JCERSAPublicKey(
        RSAPublicKey key)
    {
        this.modulus = key.getModulus();
        this.publicExponent = key.getPublicExponent();
    }

    JCERSAPublicKey(
        SubjectPublicKeyInfo    info)
    {
        try
        {
            RSAPublicKeyStructure   pubKey = new RSAPublicKeyStructure((ASN1Sequence)info.getPublicKey());

            this.modulus = pubKey.getModulus();
            this.publicExponent = pubKey.getPublicExponent();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid info structure in RSA public key");
        }
    }

    /**
     * return the modulus.
     *
     * @return the modulus.
     */
    public BigInteger getModulus()
    {
        return modulus;
    }

    /**
     * return the public exponent.
     *
     * @return the public exponent.
     */
    public BigInteger getPublicExponent()
    {
        return publicExponent;
    }

    public String getAlgorithm()
    {
        return "RSA";
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getEncoded()
    {
        SubjectPublicKeyInfo    info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, new DERNull()), new RSAPublicKeyStructure(getModulus(), getPublicExponent()).getDERObject());

        return info.getDEREncoded();
    }

    public int hashCode()
    {
        return this.getModulus().hashCode() ^ this.getPublicExponent().hashCode();
    }

    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof RSAPublicKey))
        {
            return false;
        }

        RSAPublicKey key = (RSAPublicKey)o;

        return getModulus().equals(key.getModulus())
            && getPublicExponent().equals(key.getPublicExponent());
    }

    public String toString()
    {
        StringBuffer    buf = new StringBuffer();
        String          nl = System.getProperty("line.separator");

        buf.append("RSA Public Key").append(nl);
        buf.append("            modulus: ").append(this.getModulus().toString(16)).append(nl);
        buf.append("    public exponent: ").append(this.getPublicExponent().toString(16)).append(nl);

        return buf.toString();
    }
}
