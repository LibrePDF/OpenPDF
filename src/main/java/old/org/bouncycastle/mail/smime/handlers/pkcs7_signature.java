package old.org.bouncycastle.mail.smime.handlers;

import java.awt.datatransfer.DataFlavor;

import javax.activation.ActivationDataFlavor;
import javax.mail.internet.MimeBodyPart;

public class pkcs7_signature
    extends PKCS7ContentHandler
{
    private static final ActivationDataFlavor ADF = new ActivationDataFlavor(MimeBodyPart.class, "application/pkcs7-signature", "Signature");
    private static final DataFlavor[]         DFS = new DataFlavor[] { ADF };
    
    public pkcs7_signature()
    {
        super(ADF, DFS);
    }
}
