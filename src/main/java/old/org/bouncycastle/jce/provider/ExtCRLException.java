package old.org.bouncycastle.jce.provider;

import java.security.cert.CRLException;

class ExtCRLException
    extends CRLException
{
    Throwable cause;

    ExtCRLException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
