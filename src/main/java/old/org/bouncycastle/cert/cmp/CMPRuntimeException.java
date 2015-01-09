package old.org.bouncycastle.cert.cmp;

public class CMPRuntimeException
    extends RuntimeException
{
    private Throwable cause;

    public CMPRuntimeException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}