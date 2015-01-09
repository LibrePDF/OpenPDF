package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERNull;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.DSAParameter;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import old.org.bouncycastle.crypto.params.DSAPublicKeyParameters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAParameterSpec;
import java.security.spec.DSAPublicKeySpec;

public class JDKDSAPublicKey
    implements DSAPublicKey
{
    private static final long serialVersionUID = 1752452449903495175L;

    private BigInteger      y;
    private DSAParams       dsaSpec;

    JDKDSAPublicKey(
        DSAPublicKeySpec    spec)
    {
        this.y = spec.getY();
        this.dsaSpec = new DSAParameterSpec(spec.getP(), spec.getQ(), spec.getG());
    }

    JDKDSAPublicKey(
        DSAPublicKey    key)
    {
        this.y = key.getY();
        this.dsaSpec = key.getParams();
    }

    JDKDSAPublicKey(
        DSAPublicKeyParameters  params)
    {
        this.y = params.getY();
        this.dsaSpec = new DSAParameterSpec(params.getParameters().getP(), params.getParameters().getQ(), params.getParameters().getG());
    }

    JDKDSAPublicKey(
        BigInteger        y,
        DSAParameterSpec  dsaSpec)
    {
        this.y = y;
        this.dsaSpec = dsaSpec;
    }

    JDKDSAPublicKey(
        SubjectPublicKeyInfo    info)
    {

        DERInteger              derY;

        try
        {
            derY = (DERInteger)info.getPublicKey();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid info structure in DSA public key");
        }

        this.y = derY.getValue();

        if (isNotNull(info.getAlgorithmId().getParameters()))
        {
            DSAParameter params = new DSAParameter((ASN1Sequence)info.getAlgorithmId().getParameters());
            
            this.dsaSpec = new DSAParameterSpec(params.getP(), params.getQ(), params.getG());
        }
    }

    private boolean isNotNull(DEREncodable parameters)
    {
        return parameters != null && !DERNull.INSTANCE.equals(parameters);
    }

    public String getAlgorithm()
    {
        return "DSA";
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getEncoded()
    {
        if (dsaSpec == null)
        {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa), new DERInteger(y)).getDEREncoded();
        }

        return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, new DSAParameter(dsaSpec.getP(), dsaSpec.getQ(), dsaSpec.getG()).getDERObject()), new DERInteger(y)).getDEREncoded();
    }

    public DSAParams getParams()
    {
        return dsaSpec;
    }

    public BigInteger getY()
    {
        return y;
    }

    public String toString()
    {
        StringBuffer    buf = new StringBuffer();
        String          nl = System.getProperty("line.separator");

        buf.append("DSA Public Key").append(nl);
        buf.append("            y: ").append(this.getY().toString(16)).append(nl);

        return buf.toString();
    }

    public int hashCode()
    {
        return this.getY().hashCode() ^ this.getParams().getG().hashCode() 
                ^ this.getParams().getP().hashCode() ^ this.getParams().getQ().hashCode();
    }

    public boolean equals(
        Object o)
    {
        if (!(o instanceof DSAPublicKey))
        {
            return false;
        }
        
        DSAPublicKey other = (DSAPublicKey)o;
        
        return this.getY().equals(other.getY()) 
            && this.getParams().getG().equals(other.getParams().getG()) 
            && this.getParams().getP().equals(other.getParams().getP()) 
            && this.getParams().getQ().equals(other.getParams().getQ());
    }

    private void readObject(
        ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        this.y = (BigInteger)in.readObject();
        this.dsaSpec = new DSAParameterSpec((BigInteger)in.readObject(), (BigInteger)in.readObject(), (BigInteger)in.readObject());
    }

    private void writeObject(
        ObjectOutputStream out)
        throws IOException
    {
        out.writeObject(y);
        out.writeObject(dsaSpec.getP());
        out.writeObject(dsaSpec.getQ());
        out.writeObject(dsaSpec.getG());
    }
}
