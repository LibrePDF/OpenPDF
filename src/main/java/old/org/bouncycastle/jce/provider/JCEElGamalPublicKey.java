package old.org.bouncycastle.jce.provider;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.DHPublicKeySpec;

import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.oiw.ElGamalParameter;
import old.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import old.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import old.org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import old.org.bouncycastle.jce.interfaces.ElGamalPublicKey;
import old.org.bouncycastle.jce.spec.ElGamalParameterSpec;
import old.org.bouncycastle.jce.spec.ElGamalPublicKeySpec;

public class JCEElGamalPublicKey
    implements ElGamalPublicKey, DHPublicKey
{
    static final long serialVersionUID = 8712728417091216948L;
        
    private BigInteger              y;
    private ElGamalParameterSpec    elSpec;

    JCEElGamalPublicKey(
        ElGamalPublicKeySpec    spec)
    {
        this.y = spec.getY();
        this.elSpec = new ElGamalParameterSpec(spec.getParams().getP(), spec.getParams().getG());
    }

    JCEElGamalPublicKey(
        DHPublicKeySpec    spec)
    {
        this.y = spec.getY();
        this.elSpec = new ElGamalParameterSpec(spec.getP(), spec.getG());
    }
    
    JCEElGamalPublicKey(
        ElGamalPublicKey    key)
    {
        this.y = key.getY();
        this.elSpec = key.getParameters();
    }

    JCEElGamalPublicKey(
        DHPublicKey    key)
    {
        this.y = key.getY();
        this.elSpec = new ElGamalParameterSpec(key.getParams().getP(), key.getParams().getG());
    }
    
    JCEElGamalPublicKey(
        ElGamalPublicKeyParameters  params)
    {
        this.y = params.getY();
        this.elSpec = new ElGamalParameterSpec(params.getParameters().getP(), params.getParameters().getG());
    }

    JCEElGamalPublicKey(
        BigInteger              y,
        ElGamalParameterSpec    elSpec)
    {
        this.y = y;
        this.elSpec = elSpec;
    }

    JCEElGamalPublicKey(
        SubjectPublicKeyInfo    info)
    {
        ElGamalParameter        params = new ElGamalParameter((ASN1Sequence)info.getAlgorithmId().getParameters());
        DERInteger              derY = null;

        try
        {
            derY = (DERInteger)info.getPublicKey();
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("invalid info structure in DSA public key");
        }

        this.y = derY.getValue();
        this.elSpec = new ElGamalParameterSpec(params.getP(), params.getG());
    }

    public String getAlgorithm()
    {
        return "ElGamal";
    }

    public String getFormat()
    {
        return "X.509";
    }

    public byte[] getEncoded()
    {
        SubjectPublicKeyInfo    info = new SubjectPublicKeyInfo(new AlgorithmIdentifier(OIWObjectIdentifiers.elGamalAlgorithm, new ElGamalParameter(elSpec.getP(), elSpec.getG()).getDERObject()), new DERInteger(y));

        return info.getDEREncoded();
    }

    public ElGamalParameterSpec getParameters()
    {
        return elSpec;
    }
    
    public DHParameterSpec getParams()
    {
        return new DHParameterSpec(elSpec.getP(), elSpec.getG());
    }

    public BigInteger getY()
    {
        return y;
    }

    private void readObject(
        ObjectInputStream   in)
        throws IOException, ClassNotFoundException
    {
        this.y = (BigInteger)in.readObject();
        this.elSpec = new ElGamalParameterSpec((BigInteger)in.readObject(), (BigInteger)in.readObject());
    }

    private void writeObject(
        ObjectOutputStream  out)
        throws IOException
    {
        out.writeObject(this.getY());
        out.writeObject(elSpec.getP());
        out.writeObject(elSpec.getG());
    }
}
