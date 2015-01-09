package old.org.bouncycastle.cert.crmf;

public class CRMFException
    extends Exception
{
    private Throwable cause;

    public CRMFException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}