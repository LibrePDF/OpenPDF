package old.org.bouncycastle.asn1.cmp;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class PKIMessages
    extends ASN1Encodable
{
    private ASN1Sequence content;

    private PKIMessages(ASN1Sequence seq)
    {
        content = seq;
    }

    public static PKIMessages getInstance(Object o)
    {
        if (o instanceof PKIMessages)
        {
            return (PKIMessages)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new PKIMessages((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public PKIMessages(PKIMessage msg)
    {
        content = new DERSequence(msg);
    }

    public PKIMessages(PKIMessage[] msgs)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        for (int i = 0; i < msgs.length; i++)
        {
            v.add(msgs[i]);
        }
        content = new DERSequence(v);
    }

    public PKIMessage[] toPKIMessageArray()
    {
        PKIMessage[] result = new PKIMessage[content.size()];

        for (int i = 0; i != result.length; i++)
        {
            result[i] = PKIMessage.getInstance(content.getObjectAt(i));
        }

        return result;
    }

    /**
     * <pre>
     * PKIMessages ::= SEQUENCE SIZE (1..MAX) OF PKIMessage
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        return content;
    }
}
