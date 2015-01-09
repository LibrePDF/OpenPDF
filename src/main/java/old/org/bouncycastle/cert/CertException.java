package old.org.bouncycastle.cert;

/**
 * General checked Exception thrown in the cert package and its sub-packages.
 */
public class CertException
    extends Exception
{
    private Throwable cause;

    public CertException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public CertException(String msg)
    {
        super(msg);
    }

    public Throwable getCause()
    {
        return cause;
    }
}
