package old.org.bouncycastle.asn1.esf;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * CrlValidatedID ::= SEQUENCE {
 *   crlHash OtherHash,
 *   crlIdentifier CrlIdentifier OPTIONAL }
 * </pre>
 */
public class CrlValidatedID
    extends ASN1Encodable
{

    private OtherHash crlHash;
    private CrlIdentifier crlIdentifier;

    public static CrlValidatedID getInstance(Object obj)
    {
        if (obj instanceof CrlValidatedID)
        {
            return (CrlValidatedID)obj;
        }
        else if (obj != null)
        {
            return new CrlValidatedID(ASN1Sequence.getInstance(obj));
        }

        throw new IllegalArgumentException("null value in getInstance");
    }

    private CrlValidatedID(ASN1Sequence seq)
    {
        if (seq.size() < 1 || seq.size() > 2)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                + seq.size());
        }
        this.crlHash = OtherHash.getInstance(seq.getObjectAt(0));
        if (seq.size() > 1)
        {
            this.crlIdentifier = CrlIdentifier.getInstance(seq.getObjectAt(1));
        }
    }

    public CrlValidatedID(OtherHash crlHash)
    {
        this(crlHash, null);
    }

    public CrlValidatedID(OtherHash crlHash, CrlIdentifier crlIdentifier)
    {
        this.crlHash = crlHash;
        this.crlIdentifier = crlIdentifier;
    }

    public OtherHash getCrlHash()
    {
        return this.crlHash;
    }

    public CrlIdentifier getCrlIdentifier()
    {
        return this.crlIdentifier;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.crlHash.toASN1Object());
        if (null != this.crlIdentifier)
        {
            v.add(this.crlIdentifier.toASN1Object());
        }
        return new DERSequence(v);
    }
}
