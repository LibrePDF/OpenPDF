package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.OutputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;

import old.org.bouncycastle.cms.CMSException;
import old.org.bouncycastle.cms.CMSProcessable;

/**
 * a holding class for a BodyPart to be processed.
 */
public class CMSProcessableBodyPart
    implements CMSProcessable
{
    private BodyPart   bodyPart;

    public CMSProcessableBodyPart(
        BodyPart    bodyPart)
    {
        this.bodyPart = bodyPart;
    }

    public void write(
        OutputStream out)
        throws IOException, CMSException
    {
        try
        {
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
