package old.org.bouncycastle.jce.exception;

import java.security.cert.CertPath;
import java.security.cert.CertPathValidatorException;

public class ExtCertPathValidatorException
    extends CertPathValidatorException
    implements ExtException
{

    private Throwable cause;

    public ExtCertPathValidatorException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public ExtCertPathValidatorException(String msg, Throwable cause, 
        CertPath certPath, int index)
    {
        super(msg, cause, certPath, index);
        this.cause = cause;
    }
    
    public Throwable getCause()
    {
        return cause;
    }
}
