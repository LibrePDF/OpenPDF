package old.org.bouncycastle.mail.smime.handlers;

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

public class x_pkcs7_signature 
    implements DataContentHandler 
{
    
    /*  
     *  
     *  VARIABLES
     *  
     */ 
    
    private static final ActivationDataFlavor ADF;
    private static final DataFlavor[]         ADFs;
    
    static 
    {
        ADF  = new ActivationDataFlavor(MimeBodyPart.class, "application/x-pkcs7-signature", "Signature");
        ADFs = new DataFlavor[] { ADF };
    }
    
    public Object getContent(DataSource _ds) 
        throws IOException 
    {
        return _ds.getInputStream();
    }
    
    public Object getTransferData(DataFlavor _df, DataSource _ds) 
        throws IOException 
    {    
        if (ADF.equals(_df)) 
        {
            return getContent(_ds);
        }
        else 
        {
            return null;
        }
    }
    
    public DataFlavor[] getTransferDataFlavors() 
    {
        return ADFs;
    }
    
    public void writeTo(Object _obj, String _mimeType, OutputStream _os) 
        throws IOException 
    {
        if (_obj instanceof MimeBodyPart) 
        {
            try 
            {
                ((MimeBodyPart)_obj).writeTo(_os);
            } 
            catch (MessagingException ex) 
            {
                throw new IOException(ex.getMessage());
            }
        }
        else if (_obj instanceof byte[]) 
        {
            _os.write((byte[])_obj);
        }
        else if (_obj instanceof InputStream)
        {
            int            b;
            InputStream    in = (InputStream)_obj;

            while ((b = in.read()) >= 0)
            {
                _os.write(b);
            }
        }
        else
        {
            throw new IOException("unknown object in writeTo " + _obj);
        }
    }
}
