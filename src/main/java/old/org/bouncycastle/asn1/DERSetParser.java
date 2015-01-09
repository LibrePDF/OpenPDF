package old.org.bouncycastle.asn1;

import java.io.IOException;

public class DERSetParser
    implements ASN1SetParser
{
    private ASN1StreamParser _parser;

    DERSetParser(ASN1StreamParser parser)
    {
        this._parser = parser;
    }

    public DEREncodable readObject()
        throws IOException
    {
        return _parser.readObject();
    }

    public DERObject getLoadedObject()
        throws IOException
    {
        return new DERSet(_parser.readVector(), false);
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
