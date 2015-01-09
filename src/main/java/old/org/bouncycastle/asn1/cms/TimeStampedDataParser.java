package old.org.bouncycastle.asn1.cms;

import java.io.IOException;

import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1OctetStringParser;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1SequenceParser;
import old.org.bouncycastle.asn1.BERSequence;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERInteger;
import old.org.bouncycastle.asn1.DERObject;

public class TimeStampedDataParser
{
    private DERInteger version;
    private DERIA5String dataUri;
    private MetaData metaData;
    private ASN1OctetStringParser content;
    private Evidence temporalEvidence;
    private ASN1SequenceParser parser;

    private TimeStampedDataParser(ASN1SequenceParser parser)
        throws IOException
    {
        this.parser = parser;
        this.version = DERInteger.getInstance(parser.readObject());

        DEREncodable obj = parser.readObject();

        if (obj instanceof DERIA5String)
        {
            this.dataUri = DERIA5String.getInstance(obj);
            obj = parser.readObject();
        }
        if (obj instanceof MetaData || obj instanceof ASN1SequenceParser)
        {
            this.metaData = MetaData.getInstance(obj.getDERObject());
            obj = parser.readObject();
        }
        if (obj instanceof ASN1OctetStringParser)
        {
            this.content = (ASN1OctetStringParser)obj;
        }
    }

    public static TimeStampedDataParser getInstance(Object obj)
        throws IOException
    {
        if (obj instanceof ASN1Sequence)
        {
            return new TimeStampedDataParser(((ASN1Sequence)obj).parser());
        }
        if (obj instanceof ASN1SequenceParser)
        {
            return new TimeStampedDataParser((ASN1SequenceParser)obj);
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

    public ASN1OctetStringParser getContent()
    {
        return content;
    }

    public Evidence getTemporalEvidence()
        throws IOException
    {
        if (temporalEvidence == null)
        {
            temporalEvidence = Evidence.getInstance(parser.readObject().getDERObject());
        }

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
