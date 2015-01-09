package old.org.bouncycastle.asn1.crmf;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class CertReqMessages
    extends ASN1Encodable
{
    private ASN1Sequence content;

    private CertReqMessages(ASN1Sequence seq)
    {
        content = seq;
    }

    public static CertReqMessages getInstance(Object o)
    {
        if (o instanceof CertReqMessages)
        {
            return (CertReqMessages)o;
        }

        if (o instanceof ASN1Sequence)
        {
            return new CertReqMessages((ASN1Sequence)o);
        }

        throw new IllegalArgumentException("Invalid object: " + o.getClass().getName());
    }

    public CertReqMessages(
        CertReqMsg msg)
    {
        content = new DERSequence(msg);
    }

    public CertReqMessages(
        CertReqMsg[] msgs)
    {
        ASN1EncodableVector v = new ASN1EncodableVector();
        for (int i = 0; i < msgs.length; i++) {
            v.add(msgs[i]);
        }
        content = new DERSequence(v);
    }

    public CertReqMsg[] toCertReqMsgArray()
    {
        CertReqMsg[] result = new CertReqMsg[content.size()];

        for (int i = 0; i != result.length; i++)
        {
            result[i] = CertReqMsg.getInstance(content.getObjectAt(i));
        }

        return result;
    }
    
    /**
     * <pre>
     * CertReqMessages ::= SEQUENCE SIZE (1..MAX) OF CertReqMsg
     * </pre>
     * @return a basic ASN.1 object representation.
     */
    public DERObject toASN1Object()
    {
        return content;
    }
}
