package old.org.bouncycastle.cms;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import old.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import old.org.bouncycastle.asn1.cms.CMSObjectIdentifiers;

/**
 * a holding class for a byte array of data to be processed.
 */
public class CMSAbsentContent
    implements CMSTypedData, CMSReadable
{
    private final ASN1ObjectIdentifier type;

    public CMSAbsentContent()
    {
        this(new ASN1ObjectIdentifier(CMSObjectIdentifiers.data.getId()));
    }

    public CMSAbsentContent(
        ASN1ObjectIdentifier type)
    {
        this.type = type;
    }

    public InputStream getInputStream()
    {
        return null;
    }

    public void write(OutputStream zOut)
        throws IOException, CMSException
    {
        // do nothing
    }

    public Object getContent()
    {
        return null;
    }

    public ASN1ObjectIdentifier getContentType()
    {
        return type;
    }
}
