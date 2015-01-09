package old.org.bouncycastle.asn1.x509;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERBitString;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class AttributeCertificate
    extends ASN1Encodable
{
    AttributeCertificateInfo    acinfo;
    AlgorithmIdentifier         signatureAlgorithm;
    DERBitString                signatureValue;

    /**
     * @param obj
     * @return an AttributeCertificate object
     */
    public static AttributeCertificate getInstance(Object obj)
    {
        if (obj instanceof AttributeCertificate)
        {
            return (AttributeCertificate)obj;
        }
        else if (obj != null)
        {
            return new AttributeCertificate(ASN1Sequence.getInstance(obj));
        }

        return null;
    }
    
    public AttributeCertificate(
        AttributeCertificateInfo    acinfo,
        AlgorithmIdentifier         signatureAlgorithm,
        DERBitString                signatureValue)
    {
        this.acinfo = acinfo;
        this.signatureAlgorithm = signatureAlgorithm;
        this.signatureValue = signatureValue;
    }
    
    public AttributeCertificate(
        ASN1Sequence    seq)
    {
        if (seq.size() != 3)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                    + seq.size());
        }

        this.acinfo = AttributeCertificateInfo.getInstance(seq.getObjectAt(0));
        this.signatureAlgorithm = AlgorithmIdentifier.getInstance(seq.getObjectAt(1));
        this.signatureValue = DERBitString.getInstance(seq.getObjectAt(2));
    }
    
    public AttributeCertificateInfo getAcinfo()
    {
        return acinfo;
    }

    public AlgorithmIdentifier getSignatureAlgorithm()
    {
        return signatureAlgorithm;
    }

    public DERBitString getSignatureValue()
    {
        return signatureValue;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     *  AttributeCertificate ::= SEQUENCE {
     *       acinfo               AttributeCertificateInfo,
     *       signatureAlgorithm   AlgorithmIdentifier,
     *       signatureValue       BIT STRING
     *  }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(acinfo);
        v.add(signatureAlgorithm);
        v.add(signatureValue);

        return new DERSequence(v);
    }
}
