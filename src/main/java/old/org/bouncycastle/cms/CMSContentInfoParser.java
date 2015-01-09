package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;

import old.org.bouncycastle.asn1.ASN1SequenceParser;
import old.org.bouncycastle.asn1.ASN1StreamParser;
import old.org.bouncycastle.asn1.cms.ContentInfoParser;

public class CMSContentInfoParser
{
    protected ContentInfoParser _contentInfo;
    protected InputStream       _data;

    protected CMSContentInfoParser(
        InputStream data)
        throws CMSException
    {
        _data = data;
        
        try
        {
            ASN1StreamParser in = new ASN1StreamParser(data);
    
            _contentInfo = new ContentInfoParser((ASN1SequenceParser)in.readObject());
        }
        catch (IOException e)
        {
            throw new CMSException("IOException reading content.", e);
        }
        catch (ClassCastException e)
        {
            throw new CMSException("Unexpected object reading content.", e);
        }
    }
    
    /**
     * Close the underlying data stream.
     * @throws IOException if the close fails.
     */
    public void close() throws IOException
    {
        _data.close();
    }
}
