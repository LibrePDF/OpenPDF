package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.CMSProcessable;

/**
 * a holding class for a BodyPart to be processed which does CRLF canonicalisation if
 * dealing with non-binary data.
 */
public class CMSProcessableBodyPartInbound
    implements CMSProcessable
{
    private final BodyPart    bodyPart;
    private final String      defaultContentTransferEncoding;

    /**
     * Create a processable with the default transfer encoding of 7bit 
     * 
     * @param bodyPart body part to be processed
     */
    public CMSProcessableBodyPartInbound(
        BodyPart    bodyPart)
    {
        this(bodyPart, "7bit");
    }

    /**
     * Create a processable with the a default transfer encoding of
     * the passed in value. 
     * 
     * @param bodyPart body part to be processed
     * @param defaultContentTransferEncoding the new default to use.
     */
    public CMSProcessableBodyPartInbound(
        BodyPart    bodyPart,
        String      defaultContentTransferEncoding)
    {
        this.bodyPart = bodyPart;
        this.defaultContentTransferEncoding = defaultContentTransferEncoding;
    }

    public void write(
        OutputStream out)
        throws IOException, CMSException
    {
        try
        {
            SMIMEUtil.outputBodyPart(out, bodyPart, defaultContentTransferEncoding);
        }
        catch (MessagingException e)
        {
            throw new CMSException("can't write BodyPart to stream: " + e, e);
        }
    }

    public Object getContent()
    {
        return bodyPart;
    }
}
