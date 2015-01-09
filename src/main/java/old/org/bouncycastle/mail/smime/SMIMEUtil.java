package old.org.bouncycastle.mail.smime;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import old.org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import old.org.bouncycastle.cms.CMSTypedStream;
import old.org.bouncycastle.jce.PrincipalUtil;
import old.org.bouncycastle.mail.smime.util.CRLFOutputStream;
import old.org.bouncycastle.mail.smime.util.FileBackedMimeBodyPart;

public class SMIMEUtil
{
    private static final int BUF_SIZE = 32760;
    
    static boolean isCanonicalisationRequired(
        MimeBodyPart   bodyPart,
        String defaultContentTransferEncoding) 
        throws MessagingException
    {
        String[]        cte = bodyPart.getHeader("Content-Transfer-Encoding");
        String          contentTransferEncoding;

        if (cte == null)
        {
            contentTransferEncoding = defaultContentTransferEncoding;
        }
        else
        {
            contentTransferEncoding = cte[0];
        }

        return !contentTransferEncoding.equalsIgnoreCase("binary");
    }

    public static Provider getProvider(String providerName)
        throws NoSuchProviderException
    {
        if (providerName != null)
        {
            Provider prov = Security.getProvider(providerName);

            if (prov != null)
            {
                return prov;
            }

            throw new NoSuchProviderException("provider " + providerName + " not found.");
        }

        return null;
    }

    static class LineOutputStream extends FilterOutputStream
    {
        private static byte newline[];

        public LineOutputStream(OutputStream outputstream)
        {
            super(outputstream);
        }

        public void writeln(String s)
            throws MessagingException
        {
            try
            {
                byte abyte0[] = getBytes(s);
                super.out.write(abyte0);
                super.out.write(newline);
            }
            catch(Exception exception)
            {
                throw new MessagingException("IOException", exception);
            }
        }

        public void writeln()
            throws MessagingException
        {
            try
            {
                super.out.write(newline);
            }
            catch(Exception exception)
            {
                throw new MessagingException("IOException", exception);
            }
        }

        static 
        {
            newline = new byte[2];
            newline[0] = 13;
            newline[1] = 10;
        }
        
        private static byte[] getBytes(String s)
        {
            char ac[] = s.toCharArray();
            int i = ac.length;
            byte abyte0[] = new byte[i];
            int j = 0;

            while (j < i)
            {
                abyte0[j] = (byte)ac[j++];
            }

            return abyte0;
        }
    }

    /**
     * internal preamble is generally included in signatures, while this is technically wrong,
     * if we find internal preamble we include it by default.
     */
    static void outputPreamble(LineOutputStream lOut, MimeBodyPart part, String boundary)
        throws MessagingException, IOException
    {
        InputStream in;

        try
        {
            in = part.getRawInputStream();
        }
        catch (MessagingException e)
        {
            return;   // no underlying content rely on default generation
        }

        String line;

        while ((line = readLine(in)) != null)
        {
            if (line.equals(boundary))
            {
                break;
            }

            lOut.writeln(line);
        }

        in.close();

        if (line == null)
        {
            throw new MessagingException("no boundary found");
        }
    }

    /**
     * internal postamble is generally included in signatures, while this is technically wrong,
     * if we find internal postamble we include it by default.
     */
    static void outputPostamble(LineOutputStream lOut, MimeBodyPart part, int count, String boundary)
        throws MessagingException, IOException
    {
        InputStream in;

        try
        {
            in = part.getRawInputStream();
        }
        catch (MessagingException e)
        {
            return;   // no underlying content rely on default generation
        }

        String line;
        int boundaries = count + 1;

        while ((line = readLine(in)) != null)
        {
            if (line.startsWith(boundary))
            {
                boundaries--;
  
                if (boundaries == 0)
                {
                    break;
                }
            }
        }

        while ((line = readLine(in)) != null)
        {
            lOut.writeln(line);
        }
        
        in.close();

        if (boundaries != 0)
        {
            throw new MessagingException("all boundaries not found for: " + boundary);
        }
    }

    static void outputPostamble(LineOutputStream lOut, BodyPart parent, String parentBoundary, BodyPart part)
        throws MessagingException, IOException
    {
        InputStream in;

        try
        {
            in = ((MimeBodyPart)parent).getRawInputStream();
        }
        catch (MessagingException e)
        {
            return;   // no underlying content rely on default generation
        }


        MimeMultipart multipart = (MimeMultipart)part.getContent();
        ContentType contentType = new ContentType(multipart.getContentType());
        String boundary = "--" + contentType.getParameter("boundary");
        int count = multipart.getCount() + 1;
        String line;
        while (count != 0 && (line = readLine(in)) != null)
        {
            if (line.startsWith(boundary))
            {
                count--;
            }
        }

        while ((line = readLine(in)) != null)
        {
            if (line.startsWith(parentBoundary))
            {
                break;
            }
            lOut.writeln(line);
        }

        in.close();
    }

    /*
     * read a line of input stripping of the tailing \r\n
     */
    private static String readLine(InputStream in)
        throws IOException
    {
        StringBuffer b = new StringBuffer();

        int ch;
        while ((ch = in.read()) >= 0 && ch != '\n')
        {
            if (ch != '\r')
            {
                b.append((char)ch);
            }
        }

        if (ch < 0 && b.length() == 0)
        {
            return null;
        }
        
        return b.toString();
    }

    static void outputBodyPart(
        OutputStream out,
        BodyPart     bodyPart,
        String       defaultContentTransferEncoding) 
        throws MessagingException, IOException
    {
        if (bodyPart instanceof MimeBodyPart)
        {
            MimeBodyPart    mimePart = (MimeBodyPart)bodyPart;
            String[]        cte = mimePart.getHeader("Content-Transfer-Encoding");
            String          contentTransferEncoding;

            if (mimePart.getContent() instanceof MimeMultipart)
            {
                MimeMultipart mp = (MimeMultipart)bodyPart.getContent();
                ContentType contentType = new ContentType(mp.getContentType());
                String boundary = "--" + contentType.getParameter("boundary");

                SMIMEUtil.LineOutputStream lOut = new SMIMEUtil.LineOutputStream(out);

                Enumeration headers = mimePart.getAllHeaderLines();
                while (headers.hasMoreElements())
                {
                    String header = (String)headers.nextElement();
                    lOut.writeln(header);
                }

                lOut.writeln();      // CRLF separator

                outputPreamble(lOut, mimePart, boundary);

                for (int i = 0; i < mp.getCount(); i++)
                {
                    lOut.writeln(boundary);
                    BodyPart part = mp.getBodyPart(i);
                    outputBodyPart(out, part, defaultContentTransferEncoding);
                    if (!(part.getContent() instanceof MimeMultipart))
                    {
                        lOut.writeln();       // CRLF terminator needed
                    }
                    else
                    {
                        outputPostamble(lOut, mimePart, boundary, part);
                    }
                }

                lOut.writeln(boundary + "--");
     
                outputPostamble(lOut, mimePart, mp.getCount(), boundary);

                return;
            }

            if (cte == null)
            {
                contentTransferEncoding = defaultContentTransferEncoding;
            }
            else
            {
                contentTransferEncoding = cte[0];
            }

            if (!contentTransferEncoding.equalsIgnoreCase("base64")
                   && !contentTransferEncoding.equalsIgnoreCase("quoted-printable"))
            {
                if (!contentTransferEncoding.equalsIgnoreCase("binary"))
                {
                    out = new CRLFOutputStream(out);
                }
                bodyPart.writeTo(out);
                out.flush();
                return;
            }

            boolean base64 = contentTransferEncoding.equalsIgnoreCase("base64");

            //
            // Write raw content, performing canonicalization
            //
            InputStream inRaw;

            try
            {
                inRaw = mimePart.getRawInputStream();
            }
            catch (MessagingException e)
            {
                // this is less than ideal, but if the raw output stream is unavailable it's the
                // best option we've got.
                out = new CRLFOutputStream(out);
                bodyPart.writeTo(out);
                out.flush();
                return;
            }

            //
            // Write headers
            //
            LineOutputStream outLine = new LineOutputStream(out);
            for (Enumeration e = mimePart.getAllHeaderLines(); e.hasMoreElements();) 
            {
                String header = (String)e.nextElement();
  
                outLine.writeln(header);
            }

            outLine.writeln();
            outLine.flush();


            OutputStream outCRLF;
              
            if (base64)
            {
                outCRLF = new Base64CRLFOutputStream(out);
            }
            else
            {
                outCRLF = new CRLFOutputStream(out);
            }

            byte[]      buf = new byte[BUF_SIZE];

            int len;
            while ((len = inRaw.read(buf, 0, buf.length)) > 0)
            {

                outCRLF.write(buf, 0, len);
            }

            outCRLF.flush();
        }
        else
        {
            if (!defaultContentTransferEncoding.equalsIgnoreCase("binary"))
            {
                out = new CRLFOutputStream(out);
            }

            bodyPart.writeTo(out);

            out.flush();
        }
    }

    /**
     * return the MimeBodyPart described in the raw bytes provided in content
     */
    public static MimeBodyPart toMimeBodyPart(
        byte[]    content)
        throws SMIMEException
    {
        return toMimeBodyPart(new ByteArrayInputStream(content));
    }
    
    /**
     * return the MimeBodyPart described in the input stream content
     */
    public static MimeBodyPart toMimeBodyPart(
        InputStream    content)
        throws SMIMEException
    {
        try
        {
            return new MimeBodyPart(content);
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("exception creating body part.", e);
        }
    }

    static FileBackedMimeBodyPart toWriteOnceBodyPart(
        CMSTypedStream    content)
        throws SMIMEException
    {
        try
        {
            return new WriteOnceFileBackedMimeBodyPart(content.getContentStream(), File.createTempFile("bcMail", ".mime"));
        }
        catch (IOException e)
        {
            throw new SMIMEException("IOException creating tmp file:" + e.getMessage(), e);
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("can't create part: " + e, e);
        }
    }

    /**
     * return a file backed MimeBodyPart described in {@link CMSTypedStream} content. 
     * </p>
     */
    public static FileBackedMimeBodyPart toMimeBodyPart(
        CMSTypedStream    content)
        throws SMIMEException
    {
        try
        {
            return toMimeBodyPart(content, File.createTempFile("bcMail", ".mime"));
        }
        catch (IOException e)
        {
            throw new SMIMEException("IOException creating tmp file:" + e.getMessage(), e);
        }
    }
    
    /**
     * Return a file based MimeBodyPart represented by content and backed
     * by the file represented by file.
     * 
     * @param content content stream containing body part.
     * @param file file to store the decoded body part in.
     * @return the decoded body part.
     * @throws SMIMEException
     */
    public static FileBackedMimeBodyPart toMimeBodyPart(
        CMSTypedStream    content,
        File              file)
        throws SMIMEException
    {
        try
        {
            return new FileBackedMimeBodyPart(content.getContentStream(), file);
        }
        catch (IOException e)
        {
            throw new SMIMEException("can't save content to file: " + e, e);
        }
        catch (MessagingException e)
        {
            throw new SMIMEException("can't create part: " + e, e);
        }
    }
    
    /**
     * Return a CMS IssuerAndSerialNumber structure for the passed in X.509 certificate.
     * 
     * @param cert the X.509 certificate to get the issuer and serial number for.
     * @return an IssuerAndSerialNumber structure representing the certificate.
     */
    public static IssuerAndSerialNumber createIssuerAndSerialNumberFor(
        X509Certificate cert)
        throws CertificateParsingException
    {
        try
        {
            return new IssuerAndSerialNumber(PrincipalUtil.getIssuerX509Principal(cert), cert.getSerialNumber());        
        }
        catch (Exception e)
        {
            throw new CertificateParsingException("exception extracting issuer and serial number: " + e);
        }
    }

    private static class WriteOnceFileBackedMimeBodyPart
        extends FileBackedMimeBodyPart
    {
        public WriteOnceFileBackedMimeBodyPart(InputStream content, File file)
            throws MessagingException, IOException
        {
            super(content, file);
        }

        public void writeTo(OutputStream out)
            throws MessagingException, IOException
        {
            super.writeTo(out);

            this.dispose();
        }
    }

    static class Base64CRLFOutputStream extends FilterOutputStream
    {
        protected int lastb;
        protected static byte newline[];
        private boolean isCrlfStream;

        public Base64CRLFOutputStream(OutputStream outputstream)
        {
            super(outputstream);
            lastb = -1;
        }

        public void write(int i)
            throws IOException
        {
            if (i == '\r')
            {
                out.write(newline);
            }
            else if (i == '\n')
            {
                if (lastb != '\r')
                {                                 // imagine my joy...
                    if (!(isCrlfStream && lastb == '\n'))
                    {
                        out.write(newline);
                    }
                }
                else
                {
                    isCrlfStream = true;
                }
            }
            else
            {
                out.write(i);
            }

            lastb = i;
        }

        public void write(byte[] buf)
            throws IOException
        {
            this.write(buf, 0, buf.length);
        }

        public void write(byte buf[], int off, int len)
            throws IOException
        {
            for (int i = off; i != off + len; i++)
            {
                this.write(buf[i]);
            }
        }

        public void writeln()
            throws IOException
        {
            super.out.write(newline);
        }

        static
        {
            newline = new byte[2];
            newline[0] = '\r';
            newline[1] = '\n';
        }
    }
}
