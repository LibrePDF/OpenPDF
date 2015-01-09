package old.org.bouncycastle.asn1.cms;

import old.org.bouncycastle.asn1.ASN1Encodable;
import old.org.bouncycastle.asn1.ASN1EncodableVector;
import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.ASN1Sequence;
import old.org.bouncycastle.asn1.ASN1TaggedObject;
import old.org.bouncycastle.asn1.BERSequence;
import old.org.bouncycastle.asn1.BERTaggedObject;
import old.org.bouncycastle.asn1.DEREncodable;
import old.org.bouncycastle.asn1.DERObject;

public class ContentInfo
    extends ASN1Encodable
    implements CMSObjectIdentifiers
{
    private ASN1ObjectIdentifier contentType;
    private DEREncodable        content;

    public static ContentInfo getInstance(
        Object  obj)
    {
        if (obj == null || obj instanceof ContentInfo)
        {
            return (ContentInfo)obj;
        }
        else if (obj instanceof ASN1Sequence)
        {
            return new ContentInfo((ASN1Sequence)obj);
        }

        throw new IllegalArgumentException("unknown object in factory: " + obj.getClass().getName());
    }

    public ContentInfo(
        ASN1Sequence  seq)
    {
        if (seq.size() < 1 || seq.size() > 2)
        {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        contentType = (ASN1ObjectIdentifier)seq.getObjectAt(0);

        if (seq.size() > 1)
        {
            ASN1TaggedObject tagged = (ASN1TaggedObject)seq.getObjectAt(1);
            if (!tagged.isExplicit() || tagged.getTagNo() != 0)
            {
                throw new IllegalArgumentException("Bad tag for 'content'");
            }

            content = tagged.getObject();
        }
    }

    public ContentInfo(
        ASN1ObjectIdentifier contentType,
        DEREncodable        content)
    {
        this.contentType = contentType;
        this.content = content;
    }

    public ASN1ObjectIdentifier getContentType()
    {
        return contentType;
    }

    public DEREncodable getContent()
    {
        return content;
    }

    /**
     * Produce an object suitable for an ASN1OutputStream.
     * <pre>
     * ContentInfo ::= SEQUENCE {
     *          contentType ContentType,
     *          content
     *          [0] EXPLICIT ANY DEFINED BY contentType OPTIONAL }
     * </pre>
     */
    public DERObject toASN1Object()
    {
        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(contentType);

        if (content != null)
        {
            v.add(new BERTaggedObject(0, content));
        }

        return new BERSequence(v);
    }
}
