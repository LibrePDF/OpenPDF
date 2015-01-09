package old.org.bouncycastle.operator;

public class RuntimeOperatorException
    extends RuntimeException
{
    private Throwable cause;

    public RuntimeOperatorException(String msg, Throwable cause)
    {
        super(msg);

        this.cause = cause;
    }

    public Throwable getCause()
    {
        return cause;
    }
}
