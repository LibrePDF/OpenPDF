package old.org.bouncycastle.pkcs;

import java.io.IOException;

/**
 * General IOException thrown in the cert package and its sub-packages.
 */
public class PKCSIOException
    extends IOException
{
    private Throwable cause;

    public PKCSIOException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public PKCSIOException(String msg)
    {
        super(msg);
    }

    public Throwable getCause()
    {
        return cause;
    }
}
