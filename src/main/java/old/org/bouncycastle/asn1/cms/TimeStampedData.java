package old.org.bouncycastle.asn1.cms;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetString;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.BERSequence;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;

public class TimeStampedData
    extends ASN1Encodable
{
    private DERInteger version;
    private DERIA5String dataUri;
    private MetaData metaData;
    private ASN1OctetString content;
    private Evidence temporalEvidence;

    public TimeStampedData(DERIA5String dataUri, MetaData metaData, ASN1OctetString content, Evidence temporalEvidence)
    {
        this.version = new DERInteger(1);
        this.dataUri = dataUri;
        this.metaData = metaData;
        this.content = content;
        this.temporalEvidence = temporalEvidence;
    }

    private TimeStampedData(ASN1Sequence seq)
    {
        this.version = DERInteger.getInstance(seq.getObjectAt(0));

        int index = 1;
        if (seq.getObjectAt(index) instanceof DERIA5String)
        {
            this.dataUri = DERIA5String.getInstance(seq.getObjectAt(index++));
        }
        if (seq.getObjectAt(index) instanceof MetaData || seq.getObjectAt(index) instanceof ASN1Sequence)
        {
            this.metaData = MetaData.getInstance(seq.getObjectAt(index++));
        }
        if (seq.getObjectAt(index) instanceof ASN1OctetString)
        {
            this.content = ASN1OctetString.getInstance(seq.getObjectAt(index++));
        }
        this.temporalEvidence = Evidence.getInstance(seq.getObjectAt(index));
    }

    public static TimeStampedData getInstance(Object obj)
    {
        if (obj instanceof TimeStampedData)
        {
            return (TimeStampedData)obj;
        }
        else if (obj != null)
        {
            return new TimeStampedData(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public DERIA5String getDataUri()
    {
        return dataUri;
    }

    public MetaData getMetaData()
    {
        return metaData;
    }

    public ASN1OctetString getContent()
    {
        return content;
    }

    public Evidence getTemporalEvidence()
    {
        return temporalEvidence;
    }

    /**
     * <pre>
     * TimeStampedData ::= SEQUENCE {
     *   version              INTEGER { v1(1) },
     *   dataUri              IA5String OPTIONAL,
     *   metaData             MetaData OPTIONAL,
     *   content              OCTET STRING OPTIONAL,
     *   temporalEvidence     Evidence
     * }
     * </pre>
     * @return
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(version);

        if (dataUri != null)
        {
            v.add(dataUri);
        }

        if (metaData != null)
        {
            v.add(metaData);
        }

        if (content != null)
        {
            v.add(content);
        }

        v.add(temporalEvidence);

        return new BERSequence(v);
    }
}
