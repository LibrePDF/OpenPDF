package old.org.bouncycastle.asn1;

import java.io.IOException;

public class BERApplicationSpecificParser
    implements ASN1ApplicationSpecificParser
{
    private final int tag;
    private final ASN1StreamParser parser;

    BERApplicationSpecificParser(int tag, ASN1StreamParser parser)
    {
        this.tag = tag;
        this.parser = parser;
    }

    public DEREncodable readObject()
        throws IOException
    {
        return parser.readObject();
    }

    public DERObject getLoadedObject()
        throws IOException
    {
         return new BERApplicationSpecific(tag, parser.readVector());
    }

    public DERObject getDERObject()
    {
        try
        {
            return getLoadedObject();
        }
        catch (IOException e)
        {
            throw new ASN1ParsingException(e.getMessage(), e);
        }
    }

}
