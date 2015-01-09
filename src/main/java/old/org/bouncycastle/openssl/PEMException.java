package old.org.bouncycastle.openssl;

import java.io.IOException;

public class PEMException
    extends IOException
{
    Exception    underlying;

    public PEMException(
        String    message)
    {
        super(message);
    }

    public PEMException(
        String        message,
        Exception    underlying)
    {
        super(message);
        this.underlying = underlying;
    }

    public Exception getUnderlyingException()
    {
        return underlying;
    }


    public Throwable getCause()
    {
        return underlying;
    }
}
