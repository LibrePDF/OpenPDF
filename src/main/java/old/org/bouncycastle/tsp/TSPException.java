package old.org.bouncycastle.tsp;

public class TSPException
    extends Exception
{
    Exception underlyingException;

    public TSPException(String message)
    {
        super(message);
    }

    public TSPException(String message, Exception e)
    {
        super(message);
        underlyingException = e;
    }

    public Exception getUnderlyingException()
    {
        return underlyingException;
    }

    public Throwable getCause()
    {
        return underlyingException;
    }
}
