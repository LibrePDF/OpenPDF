package old.org.bouncycastle.asn1.cms;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.DERBoolean;
import old.org.bouncycastle.asn1.DERIA5String;
import old.org.bouncycastle.asn1.DERObject;
import old.org.bouncycastle.asn1.DERSequence;
import old.org.bouncycastle.asn1.DERUTF8String;

public class MetaData
    extends ASN1Encodable
{
    private DERBoolean hashProtected;
    private DERUTF8String fileName;
    private DERIA5String  mediaType;
    private Attributes otherMetaData;

    public MetaData(
        DERBoolean hashProtected,
        DERUTF8String fileName,
        DERIA5String mediaType,
        Attributes otherMetaData)
    {
        this.hashProtected = hashProtected;
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.otherMetaData = otherMetaData;
    }

    private MetaData(ASN1Sequence seq)
    {
        this.hashProtected = DERBoolean.getInstance(seq.getObjectAt(0));

        int index = 1;

        if (index < seq.size() && seq.getObjectAt(index) instanceof DERUTF8String)
        {
            this.fileName = DERUTF8String.getInstance(seq.getObjectAt(index++));
        }
        if (index < seq.size() && seq.getObjectAt(index) instanceof DERIA5String)
        {
            this.mediaType = DERIA5String.getInstance(seq.getObjectAt(index++));
        }
        if (index < seq.size())
        {
            this.otherMetaData = Attributes.getInstance(seq.getObjectAt(index++));
        }
    }

    public static MetaData getInstance(Object obj)
    {
        if (obj instanceof MetaData)
        {
            return (MetaData)obj;
        }
        else if (obj != null)
        {
            return new MetaData(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    /**
     * <pre>
     * MetaData ::= SEQUENCE {
     *   hashProtected        BOOLEAN,
     *   fileName             UTF8String OPTIONAL,
     *   mediaType            IA5String OPTIONAL,
     *   otherMetaData        Attributes OPTIONAL
     * }
     * </pre>
     * @return
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector v = new ASN1EncodableVector();

        v.add(hashProtected);

        if (fileName != null)
        {
            v.add(fileName);
        }

        if (mediaType != null)
        {
            v.add(mediaType);
        }

        if (otherMetaData != null)
        {
            v.add(otherMetaData);
        }
        
        return new DERSequence(v);
    }

    public boolean isHashProtected()
    {
        return hashProtected.isTrue();
    }

    public DERUTF8String getFileName()
    {
        return this.fileName;
    }

    public DERIA5String getMediaType()
    {
        return this.mediaType;
    }

    public Attributes getOtherMetaData()
    {
        return otherMetaData;
    }
}
