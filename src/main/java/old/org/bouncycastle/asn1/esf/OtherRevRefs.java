package old.org.bouncycastle.asn1.esf;

import java.io.IOException;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Object;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * OtherRevRefs ::= SEQUENCE {
 *   otherRevRefType OtherRevRefType,
 *   otherRevRefs ANY DEFINED BY otherRevRefType
 * }
 *
 * OtherRevRefType ::= OBJECT IDENTIFIER
 * </pre>
 */
public class OtherRevRefs
    extends ASN1Encodable
{

    private ASN1ObjectIdentifier otherRevRefType;
    private ASN1Object otherRevRefs;

    public static OtherRevRefs getInstance(Object obj)
    {
        if (obj instanceof OtherRevRefs)
        {
            return (OtherRevRefs)obj;
        }
        else if (obj != null)
        {
            return new OtherRevRefs(ASN1Sequence.getInstance(obj));
        }

        throw new IllegalArgumentException("null value in getInstance");
    }

    private OtherRevRefs(ASN1Sequence seq)
    {
        if (seq.size() != 2)
        {
            throw new IllegalArgumentException("Bad sequence size: "
                + seq.size());
        }
        this.otherRevRefType = new ASN1ObjectIdentifier(((DERObjectIdentifier)seq.getObjectAt(0)).getId());
        try
        {
            this.otherRevRefs = ASN1Object.fromByteArray(seq.getObjectAt(1)
                .getDERObject().getDEREncoded());
        }
        catch (IOException e)
        {
            throw new IllegalStateException();
        }
    }

    public ASN1ObjectIdentifier getOtherRevRefType()
    {
        return this.otherRevRefType;
    }

    public ASN1Object getOtherRevRefs()
    {
        return this.otherRevRefs;
    }

    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(this.otherRevRefType);
        v.add(this.otherRevRefs);
        return new DERSequence(v);
    }
}
