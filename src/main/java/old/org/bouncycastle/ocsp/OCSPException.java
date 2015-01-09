package old.org.bouncycastle.ocsp;

public class OCSPException
    extends Exception
{
    Exception   e;

    public OCSPException(
        String name)
    {
        super(name);
    }

    public OCSPException(
        String name,
        Exception e)
    {
        super(name);

        this.e = e;
    }

    public Exception getUnderlyingException()
    {
        return e;
    }

    public Throwable getCause()
    {
        return e;
    }
}
