package old.org.bouncycastle.x509;

import java.security.cert.CertPath;

import old.org.bouncycastle.i18n.ErrorBundle;
import old.org.bouncycastle.i18n.LocalizedException;

public class CertPathReviewerException extends LocalizedException
{

    private int index = -1;
    
    private CertPath certPath = null;
    
    public CertPathReviewerException(ErrorBundle errorMessage, Throwable throwable)
    {
        super(errorMessage, throwable);
    }

    public CertPathReviewerException(ErrorBundle errorMessage)
    {
        super(errorMessage);
    }

    public CertPathReviewerException(
            ErrorBundle errorMessage, 
            Throwable throwable,
            CertPath certPath,
            int index)
    {
        super(errorMessage, throwable);
        if (certPath == null || index == -1)
        {
            throw new IllegalArgumentException();
        }
        if (index < -1 || (certPath != null && index >= certPath.getCertificates().size()))
        {
            throw new IndexOutOfBoundsException();
        }
        this.certPath = certPath;
        this.index = index;
    }
    
    public CertPathReviewerException(
            ErrorBundle errorMessage, 
            CertPath certPath,
            int index)
    {
        super(errorMessage);
        if (certPath == null || index == -1)
        {
            throw new IllegalArgumentException();
        }
        if (index < -1 || (certPath != null && index >= certPath.getCertificates().size()))
        {
            throw new IndexOutOfBoundsException();
        }
        this.certPath = certPath;
        this.index = index;
    }
    
    public CertPath getCertPath()
    {
        return certPath;
    }
    
    public int getIndex()
    {
        return index;
    }

}
