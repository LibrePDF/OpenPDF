package old.org.bouncycastle.asn1.cms;

import java.util.Enumeration;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;

public class TimeStampTokenEvidence
    extends ASN1Encodable
{
    private TimeStampAndCRL[] timeStampAndCRLs;

    public TimeStampTokenEvidence(TimeStampAndCRL[] timeStampAndCRLs)
    {
        this.timeStampAndCRLs = timeStampAndCRLs;
    }

    public TimeStampTokenEvidence(TimeStampAndCRL timeStampAndCRL)
    {
        this.timeStampAndCRLs = new TimeStampAndCRL[1];

        timeStampAndCRLs[0] = timeStampAndCRL;
    }

    private TimeStampTokenEvidence(ASN1Sequence seq)
    {
        this.timeStampAndCRLs = new TimeStampAndCRL[seq.size()];

        int count = 0;

        for (Enumeration en = seq.getObjects(); en.hasMoreElements();)
        {
            timeStampAndCRLs[count++] = TimeStampAndCRL.getInstance(en.nextElement());
        }
    }

    public static TimeStampTokenEvidence getInstance(ASN1TaggedObject tagged, boolean explicit)
    {
        return getInstance(ASN1Sequence.getInstance(tagged, explicit));
    }

    public static TimeStampTokenEvidence getInstance(Object obj)
    {
        if (obj instanceof TimeStampTokenEvidence)
        {
            return (TimeStampTokenEvidence)obj;
        }
        else if (obj != null)
        {
            return new TimeStampTokenEvidence(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public TimeStampAndCRL[] toTimeStampAndCRLArray()
    {
        return timeStampAndCRLs;
    }
    
    /**
     * <pre>
     * TimeStampTokenEvidence ::=
     *    SEQUENCE SIZE(1..MAX) OF TimeStampAndCRL
     * </pre>
     * @return
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        for (int i = 0; i != timeStampAndCRLs.length; i++)
        {
            v.add(timeStampAndCRLs[i]);
        }

        return new DERSequence(v);
    }

}
