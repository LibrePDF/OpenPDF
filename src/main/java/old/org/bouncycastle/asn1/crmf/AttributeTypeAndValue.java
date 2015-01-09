package old.org.bouncycastle.asn1.crmf;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERObjectIdentifier;
import old.org.bouncycastle.asn1.DERSequence;

public class AttributeTypeAndValue
    extends ASN1Encodable
{
    private DERObjectIdentifier type;
    private ASN1Encodable       value;

    private AttributeTypeAndValue(ASN1Sequence seq)
    {
        type = (DERObjectIdentifier)seq.getObjectAt(0);
        value = (ASN1Encodable)seq.getObjectAt(1);
    }

    public static AttributeTypeAndValue getInstance(Object o)
    {
        if (o instanceof AttributeTypeAndValue)
        {
            return (AttributeTypeAndValue)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new AttributeTypeAndValue((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public AttributeTypeAndValue(
        String oid,
        ASN1Encodable value)
    {
        this(new DERObjectIdentifier(oid), value);
    }

    public AttributeTypeAndValue(
        DERObjectIdentifier type,
        ASN1Encodable value)
    {
        this.type = type;
        this.value = value;
    }

    public DERObjectIdentifier getType()
    {
        return type;
    }

    public ASN1Encodable getValue()
    {
        return value;
    }

    /**
     * <pre>
     * AttributeTypeAndValue ::= SEQUENCE {
     *           type         OBJECT IDENTIFIER,
     *           value        ANY DEFINED BY type }
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(type);
        v.add(value);

        return new DERSequence(v);
    }
}
