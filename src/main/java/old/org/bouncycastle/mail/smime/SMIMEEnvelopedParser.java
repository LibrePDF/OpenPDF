package old.org.bouncycastle.mail.smime;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

import old.org.bouncycastle.cms.CMSEnvelopedDataParser;
import old.org.bouncycastle.cms.CMSException;

/**
 * Stream based containing class for an S/MIME pkcs7-mime encrypted MimePart.
 */
public class SMIMEEnvelopedParser
    extends CMSEnvelopedDataParser
{
    private final MimePart message;

    private static InputStream getInputStream(
        Part    bodyPart,
        int     bufferSize)
        throws MessagingException
    {
        try
        {
            InputStream in = bodyPart.getInputStream();
            
            if (bufferSize == 0)
            {
                return new BufferedInputStream(in);
            }
            else
            {
                return new BufferedInputStream(in, bufferSize);
            }
        }
        catch (IOException e)
        {
            throw new MessagingException("can't extract input stream: " + e);
        }
    }

    public SMIMEEnvelopedParser(
        MimeBodyPart    message) 
        throws IOException, MessagingException, CMSException
    {
        this(message, 0);
    }

    public SMIMEEnvelopedParser(
        MimeMessage    message) 
        throws IOException, MessagingException, CMSException
    {
        this(message, 0);
    }
    
    /**
     * Create a parser from a MimeBodyPart using the passed in buffer size
     * for reading it.
     * 
     * @param message body part to be parsed.
     * @param bufferSize bufferSoze to be used.
     */
    public SMIMEEnvelopedParser(
        MimeBodyPart    message,
        int             bufferSize) 
        throws IOException, MessagingException, CMSException
    {
        super(getInputStream(message, bufferSize));

        this.message = message;
    }

    /**
     * Create a parser from a MimeMessage using the passed in buffer size
     * for reading it.
     * 
     * @param message message to be parsed.
     * @param bufferSize bufferSoze to be used.
     */
    public SMIMEEnvelopedParser(
        MimeMessage    message,
        int            bufferSize) 
        throws IOException, MessagingException, CMSException
    {
        super(getInputStream(message, bufferSize));

        this.message = message;
    }

    public MimePart getEncryptedContent()
    {
        return message;
    }
}
