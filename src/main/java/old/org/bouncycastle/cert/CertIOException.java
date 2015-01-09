package old.org.bouncycastle.cert;

import java.io.IOException;

/**
 * General IOException thrown in the cert package and its sub-packages.
 */
public class CertIOException
    extends IOException
{
    private Throwable cause;

    public CertIOException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public CertIOException(String msg)
    {
        super(msg);
    }

    public Throwable getCause()
    {
        return cause;
    }
}
