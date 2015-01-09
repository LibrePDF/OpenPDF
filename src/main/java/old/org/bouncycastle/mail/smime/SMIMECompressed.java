package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

import old.org.bouncycastle.cms.CMSCompressedData;
import old.org.bouncycastle.cms.CMSException;

/**
 * containing class for an S/MIME pkcs7-mime MimePart.
 */
public class SMIMECompressed
    extends CMSCompressedData
{
    MimePart                message;

    private static InputStream getInputStream(
        Part    bodyPart)
        throws MessagingException
    {
        try
        {
            return bodyPart.getInputStream();
        }
        catch (IOException e)
        {
            throw new MessagingException("can't extract input stream: " + e);
        }
    }

    public SMIMECompressed(
        MimeBodyPart    message) 
        throws MessagingException, CMSException
    {
        super(getInputStream(message));

        this.message = message;
    }

    public SMIMECompressed(
        MimeMessage    message) 
        throws MessagingException, CMSException
    {
        super(getInputStream(message));

        this.message = message;
    }

    public MimePart getCompressedContent()
    {
        return message;
    }
}
