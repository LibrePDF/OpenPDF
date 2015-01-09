package old.org.bouncycastle.asn1;

import java.io.IOException;
import java.io.InputStream;

public class BERTaggedObjectParser
    implements ASN1TaggedObjectParser
{
    private boolean _constructed;
    private int _tagNumber;
    private ASN1StreamParser _parser;

    /**
     * @deprecated
     */
    protected BERTaggedObjectParser(
        int         baseTag,
        int         tagNumber,
        InputStream contentStream)
    {
        this((baseTag & DERTags.CONSTRUCTED) != 0, tagNumber, new ASN1StreamParser(contentStream));
    }

    BERTaggedObjectParser(
        boolean             constructed,
        int                 tagNumber,
        ASN1StreamParser    parser)
    {
        _constructed = constructed;
        _tagNumber = tagNumber;
        _parser = parser;
    }

    public boolean isConstructed()
    {
        return _constructed;
    }

    public int getTagNo()
    {
        return _tagNumber;
    }

    public DEREncodable getObjectParser(
        int     tag,
        boolean isExplicit)
        throws IOException
    {
        if (isExplicit)
        {
            if (!_constructed)
            {
                throw new IOException("Explicit tags must be constructed (see X.690 8.14.2)");
            }
            return _parser.readObject();
        }

        return _parser.readImplicit(_constructed, tag);
    }

    public DERObject getLoadedObject()
        throws IOException
    {
        return _parser.readTaggedObject(_constructed, _tagNumber);
    }

    public DERObject getDERObject()
    {
        try
        {
            return this.getLoadedObject();
        }
        catch (IOException e)
        {
            throw new ASN1ParsingException(e.getMessage());
        }
    }
}
