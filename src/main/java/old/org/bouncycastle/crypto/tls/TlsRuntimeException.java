package old.org.bouncycastle.crypto.tls;

public class TlsRuntimeException extends RuntimeException
{
    private static final long serialVersionUID = 1928023487348344086L;

    Throwable e;

    public TlsRuntimeException(String message, Throwable e)
    {
        super(message);

        this.e = e;
    }

    public TlsRuntimeException(String message)
    {
        super(message);
    }

    public Throwable getCause()
    {
        return e;
    }
}
