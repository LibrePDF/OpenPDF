package old.org.bouncycastle.asn1.pkcs;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1InputStream;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1Set;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DEROctetString;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.DERTaggedObject;
import old.org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class PrivateKeyInfo
    extends ASN1Encodable
{
    private DERObject               privKey;
    private AlgorithmIdentifier     algId;
    private ASN1Set                 attributes;

    public static PrivateKeyInfo getInstance(
        ASN1TaggedObject obj,
        boolean          explicit)
    {
        return getInstance(ASN1Sequence.getInstance(obj, explicit));
    }

    public static PrivateKeyInfo getInstance(
        Object  obj)
    {
        if (obj instanceof PrivateKeyInfo)
        {
            return (PrivateKeyInfo)obj;
        }
        else if (obj != null)
        {
            return new PrivateKeyInfo(ASN1Sequence.getInstance(obj));
        }

        return null;
    }
        
    public PrivateKeyInfo(
        AlgorithmIdentifier algId,
        DERObject           privateKey)
    {
        this(algId, privateKey, null);
    }

    public PrivateKeyInfo(
        AlgorithmIdentifier algId,
        DERObject           privateKey,
        ASN1Set             attributes)
    {
        this.privKey = privateKey;
        this.algId = algId;
        this.attributes = attributes;
    }

    public PrivateKeyInfo(
        ASN1Sequence  seq)
    {
        Enumeration e = seq.getObjects();

        BigInteger  version = ((DERInteger)e.nextElement()).getValue();
        if (version.intValue() != 0)
        {
            throw new IllegalArgumentException("wrong version for private key info");
        }

        algId = new AlgorithmIdentifier((ASN1Sequence)e.nextElement());

        try
        {
            ASN1InputStream         aIn = new ASN1InputStream(((ASN1OctetString)e.nextElement()).getOctets());

            privKey = aIn.readObject();
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException("Error recoverying private key from sequence");
        }
        
        if (e.hasMoreElements())
        {
           attributes = ASN1Set.getInstance((ASN1TaggedObject)e.nextElement(), false);
        }
    }

    public AlgorithmIdentifier getAlgorithmId()
    {
        return algId;
    }

    public DERObject getPrivateKey()
    {
        return privKey;
    }
    
    public ASN1Set getAttributes()
    {
        return attributes;
    }

    /**
     * write out an RSA private key with its associated information
     * as described in PKCS8.
     * <pre>
     *      PrivateKeyInfo ::= SEQUENCE {
     *                              version Version,
     *                              privateKeyAlgorithm AlgorithmIdentifier {{PrivateKeyAlgorithms}},
     *                              privateKey PrivateKey,
     *                              attributes [0] IMPLICIT Attributes OPTIONAL 
     *                          }
     *      Version ::= INTEGER {v1(0)} (v1,...)
     *
     *      PrivateKey ::= OCTET STRING
     *
     *      Attributes ::= SET OF Attribute
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(new DERInteger(0));
        v.add(algId);
        v.add(new DEROctetString(privKey));

        if (attributes != null)
        {
            v.add(new DERTaggedObject(false, 0, attributes));
        }
        
        return new DERSequence(v);
    }
}
