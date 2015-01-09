package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.CMSProcessable;
import old.org.bouncycastle.mail.smime.util.CRLFOutputStream;

/**
 * a holding class for a BodyPart to be processed which does CRLF canocicalisation if 
 * dealing with non-binary data.
 */
public class CMSProcessableBodyPartOutbound
    implements CMSProcessable
{
    private BodyPart   bodyPart;
    private String     defaultContentTransferEncoding;

    /**
     * Create a processable with the default transfer encoding of 7bit 
     * 
     * @param bodyPart body part to be processed
     */
    public CMSProcessableBodyPartOutbound(
        BodyPart    bodyPart)
    {
        this.bodyPart = bodyPart;
    }

    /**
     * Create a processable with the a default transfer encoding of
     * the passed in value. 
     * 
     * @param bodyPart body part to be processed
     * @param defaultContentTransferEncoding the new default to use.
     */
    public CMSProcessableBodyPartOutbound(
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
            if (SMIMEUtil.isCanonicalisationRequired((MimeBodyPart)bodyPart, defaultContentTransferEncoding))
            {
                out = new CRLFOutputStream(out);
            }
            
            bodyPart.writeTo(out);
        }
        catch (MessagingException e)
        {
            throw new CMSException("can't write BodyPart to stream.", e);
        }
    }

    public Object getContent()
    {
        return bodyPart;
    }
}
