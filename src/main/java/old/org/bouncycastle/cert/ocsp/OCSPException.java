package old.org.bouncycastle.cert.ocsp;

public class OCSPException
    extends Exception
{
    private Throwable   cause;

    public OCSPException(
        String name)
    {
        super(name);
    }

    public OCSPException(
        String name,
        Throwable cause)
    {
        super(name);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
