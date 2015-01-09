package old.org.bouncycastle.mail.smime;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import old.org.bouncycastle.cms.CMSEnvelopedGenerator;
import old.org.bouncycastle.util.Strings;

/**
 * super class of the various generators.
 */
public class SMIMEGenerator
{
    private static Map BASE_CIPHER_NAMES = new HashMap();
    
    static
    {
        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.DES_EDE3_CBC,  "DESEDE");
        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.AES128_CBC,  "AES");
        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.AES192_CBC,  "AES");
        BASE_CIPHER_NAMES.put(CMSEnvelopedGenerator.AES256_CBC,  "AES");
    }

    protected boolean                     useBase64 = true;
    protected String                      encoding = "base64";  // default sets base64

    /**
     * base constructor
     */
    protected SMIMEGenerator()
    {
    }

    /**
     * set the content-transfer-encoding for the signature.
     */
    public void setContentTransferEncoding(
        String  encoding)
    {
        this.encoding = encoding;
        this.useBase64 = Strings.toLowerCase(encoding).equals("base64");
    }

    /**
     * Make sure we have a valid content body part - setting the headers
     * with defaults if neccessary.
     */
    protected MimeBodyPart makeContentBodyPart(
        MimeBodyPart    content)
        throws SMIMEException
    {
        //
        // add the headers to the body part - if they are missing, in
        // the event they have already been set the content settings override
        // any defaults that might be set.
        //
        try
        {
            MimeMessage     msg = new MimeMessage((Session)null);

            Enumeration     e = content.getAllHeaders();

            msg.setDataHandler(content.getDataHandler());

            while (e.hasMoreElements())
            {
                Header  hdr =(Header)e.nextElement();

                msg.setHeader(hdr.getName(), hdr.getValue());
            }

            msg.saveChanges();

            //
            // we do this to make sure at least the default headers are
            // set in the body part.
            //
            e = msg.getAllHeaders();

            while (e.hasMoreElements())
            {
                Header  hdr =(Header)e.nextElement();

                if (Strings.toLowerCase(hdr.getName()).startsWith("content-"))
                {
                    content.setHeader(hdr.getName(), hdr.getValue());
                }
            }
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception saving message state.", e);
        }

        return content;
    }

    /**
     * extract an appropriate body part from the passed in MimeMessage
     */
    protected MimeBodyPart makeContentBodyPart(
        MimeMessage     message)
        throws SMIMEException
    {
        MimeBodyPart    content = new MimeBodyPart();

        //
        // add the headers to the body part.
        //
        try
        {
            message.removeHeader("Message-Id");
            message.removeHeader("Mime-Version");

            // JavaMail has a habit of reparsing some content types, if the bodypart is
            // a multipart it might be signed, we rebuild the body part using the raw input stream for the message.
            try
            {
                if (message.getContent() instanceof Multipart)
                {
                    content.setContent(message.getRawInputStream(), message.getContentType());

                    extractHeaders(content, message);
                    
                    return content;
                }
            }
            catch (MessagingException e)
            {
                // fall back to usual method below
            }
       
            content.setContent(message.getContent(), message.getContentType());

            content.setDataHandler(message.getDataHandler());

            extractHeaders(content, message);
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception saving message state.", e);
        }
        catch (IOException e)
        {
            throw new SMIMEException("exception getting message content.", e);
        }

        return content;
    }

    private void extractHeaders(MimeBodyPart content, MimeMessage message)
        throws MessagingException
    {
        Enumeration e = message.getAllHeaders();
        
        while (e.hasMoreElements())
        {
            Header hdr =(Header)e.nextElement();

            content.addHeader(hdr.getName(), hdr.getValue());
        }
    }

    protected KeyGenerator createSymmetricKeyGenerator(
        String encryptionOID,
        Provider provider)
    throws NoSuchAlgorithmException
    {
        try
        {
            return createKeyGenerator(encryptionOID, provider);
        }
        catch (NoSuchAlgorithmException e)
        {
            try
            {
                String algName = (String)BASE_CIPHER_NAMES.get(encryptionOID);
                if (algName != null)
                {
                    return createKeyGenerator(algName, provider);
                }
            }
            catch (NoSuchAlgorithmException ex)
            {
                // ignore
            }
            if (provider != null)
            {
                return createSymmetricKeyGenerator(encryptionOID, null);
            }
            throw e;
        }
    }

    private KeyGenerator createKeyGenerator(
        String algName,
        Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider != null)
        {
            return KeyGenerator.getInstance(algName, provider);
        }
        else
        {
            return KeyGenerator.getInstance(algName);
        }
    }
}
