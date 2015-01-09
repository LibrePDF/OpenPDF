package old.org.bouncycastle.mail.smime.examples;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

public class ExampleUtils
{
    /**
     * Dump the content of the passed in BodyPart to the file fileName.
     *
     * @throws MessagingException
     * @throws IOException
     */
    public static void dumpContent(
        MimeBodyPart bodyPart, 
        String fileName) 
        throws MessagingException, IOException
    {
        //
        // print mime type of compressed content
        //
        System.out.println("content type: " + bodyPart.getContentType());
        
        //
        // recover the compressed content
        //
        OutputStream out = new FileOutputStream(fileName);
        InputStream in = bodyPart.getInputStream();
        
        byte[] buf = new byte[10000];
        int    len;
        
        while ((len = in.read(buf, 0, buf.length)) > 0)
        {
            out.write(buf, 0, len);
        }
        
        out.close();
    }
    
    public static String findKeyAlias(
        KeyStore store, 
        String storeName, 
        char[] password) 
        throws Exception
    {
        store.load(new FileInputStream(storeName), password);
    
        Enumeration e = store.aliases();
        String      keyAlias = null;
    
        while (e.hasMoreElements())
        {
            String  alias = (String)e.nextElement();
    
            if (store.isKeyEntry(alias))
            {
                keyAlias = alias;
            }
        }
    
        if (keyAlias == null)
        {
            throw new IllegalArgumentException("can't find a private key in keyStore: " + storeName);
        }
        
        return keyAlias;
    }
}
