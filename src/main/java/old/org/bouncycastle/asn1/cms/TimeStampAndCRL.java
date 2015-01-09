package old.org.bouncycastle.asn1.cms;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.x509.CertificateList;

public class TimeStampAndCRL
    extends ASN1Encodable
{
    private ContentInfo timeStamp;
    private CertificateList crl;

    public TimeStampAndCRL(ContentInfo timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    private TimeStampAndCRL(ASN1Sequence seq)
    {
        this.timeStamp = ContentInfo.getInstance(seq.getObjectAt(0));
        if (seq.size() == 2)
        {
            this.crl = CertificateList.getInstance(seq.getObjectAt(1));
        }
    }

    public static TimeStampAndCRL getInstance(Object obj)
    {
        if (obj instanceof TimeStampAndCRL)
        {
            return (TimeStampAndCRL)obj;
        }
        else if (obj != null)
        {
            return new TimeStampAndCRL(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public ContentInfo getTimeStampToken()
    {
        return this.timeStamp;
    }

    public CertificateList getCertificateList()
    {
        return this.crl;
    }

    /**
     * <pre>
     * TimeStampAndCRL ::= SEQUENCE {
     *     timeStamp   TimeStampToken,          -- according to RFC 3161
     *     crl         CertificateList OPTIONAL -- according to RFC 5280
     *  }
     * </pre>
     * @return
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(timeStamp);

        if (crl != null)
        {
            v.add(crl);
        }

        return new DERSequence(v);
    }
}
