package old.org.bouncycastle.asn1.crmf;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class Controls
    extends ASN1Encodable
{
    private ASN1Sequence content;

    private Controls(ASN1Sequence seq)
    {
        content = seq;
    }

    public static Controls getInstance(Object o)
    {
        if (o instanceof Controls)
        {
            return (Controls)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new Controls((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public Controls(AttributeTypeAndValue atv)
    {
        content = new DERSequence(atv);
    }

    public Controls(AttributeTypeAndValue[] atvs)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        for (int i = 0; i < atvs.length; i++) {
            v.add(atvs[i]);
        }
        content = new DERSequence(v);
    }

    public AttributeTypeAndValue[] toAttributeTypeAndValueArray()
    {
        AttributeTypeAndValue[] result = new AttributeTypeAndValue[content.size()];

        for (int i = 0; i != result.length; i++)
        {
            result[i] = AttributeTypeAndValue.getInstance(content.getObjectAt(i));
        }

        return result;
    }

    /**
     * <pre>
     * Controls  ::= SEQUENCE SIZE(1..MAX) OF AttributeTypeAndValue
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        return content;
    }
}
