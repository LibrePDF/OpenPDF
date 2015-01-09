package old.org.bouncycastle.cert.cmp;

public class CMPException
    extends Exception
{
    private Throwable cause;

    public CMPException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public CMPException(String msg)
    {
        super(msg);
    }

    public Throwable getCause()
    {
        return cause;
    }
}