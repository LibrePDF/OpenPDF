package old.org.bouncycastle.mail.smime.util;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

public class FileBackedMimeBodyPart 
    extends MimeBodyPart
{
    private static final int BUF_SIZE = 32760;

    private final File _file;

    /**
     * Create a MimeBodyPart backed by the data in file.
     *
     * @param file file containing the body part.
     * @throws MessagingException an exception occurs parsing file.
     * @throws IOException an exception occurs accessing file.
     */
    public FileBackedMimeBodyPart(
        File file)
        throws MessagingException, IOException
    {
        super(new SharedFileInputStream(file));

        _file = file;
    }

    /**
     * Create a MimeBodyPart backed by file based on the headers and
     * content data in content.
     *
     * @param content an inputstream containing the body part.
     * @param file a handle to the backing file to use for storage.
     * @throws MessagingException an exception occurs parsing the resulting body part in file.
     * @throws IOException an exception occurs accessing file or content.
     */
    public FileBackedMimeBodyPart(
        InputStream content,
        File file)
        throws MessagingException, IOException
    {
        this(saveStreamToFile(content, file));
    }

    /**
     * Create a MimeBodyPart backed by file, with the headers
     * given in headers and body content taken from the stream body.
     *
     * @param headers headers for the body part.
     * @param body internal content for the body part.
     * @param file backing file to use.
     *
     * @throws MessagingException if the body part can't be produced.
     * @throws IOException if there is an issue reading stream or writing to file.
     */
    public FileBackedMimeBodyPart(
        InternetHeaders headers,
        InputStream body,
        File file)
        throws MessagingException, IOException
    {
        this(saveStreamToFile(headers, body, file));
    }

    public void writeTo(
        OutputStream out)
        throws IOException, MessagingException
    {
        if (!_file.exists())
        {
            throw new IOException("file " + _file.getCanonicalPath() + " no longer exists.");
        }

        super.writeTo(out);
    }

    /**
     * Close off the underlying shared streams and remove the backing file.
     *
     * @throws IOException if streams cannot be closed or the file cannot be deleted.
     */
    public void dispose() 
        throws IOException
    {
        ((SharedFileInputStream)contentStream).getRoot().dispose();
        
        if (_file.exists() && !_file.delete())
        {
            throw new IOException("deletion of underlying file <" + _file.getCanonicalPath() + "> failed.");
        }
    }

    private static File saveStreamToFile(InputStream content, File tempFile)
        throws IOException
    {
        saveContentToStream(new FileOutputStream(tempFile), content);

        return tempFile;
    }

    private static File saveStreamToFile(InternetHeaders headers, InputStream content, File tempFile)
        throws IOException
    {
        OutputStream out = new FileOutputStream(tempFile);
        Enumeration en = headers.getAllHeaderLines();

        while (en.hasMoreElements())
        {
            writeHeader(out, (String)en.nextElement());
        }

        writeSeperator(out);

        saveContentToStream(out, content);

        return tempFile;
    }


    private static void writeHeader(OutputStream out, String header)
        throws IOException
    {
        for (int i = 0; i != header.length(); i++)
        {
            out.write(header.charAt(i));
        }

        writeSeperator(out);
    }

     private static void writeSeperator(OutputStream out)
         throws IOException
     {
         out.write('\r');
         out.write('\n');
     }

     private static void saveContentToStream(
        OutputStream out,
        InputStream content)
        throws IOException
    {
        byte[] buf = new byte[BUF_SIZE];
        int    len;

        while ((len = content.read(buf, 0, buf.length)) > 0)
        {
            out.write(buf, 0, len);
        }

        out.close();
        content.close();
    }
 }
