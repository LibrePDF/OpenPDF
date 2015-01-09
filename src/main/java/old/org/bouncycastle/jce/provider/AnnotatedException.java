package old.org.bouncycastle.jce.provider;

import old.org.bouncycastle.jce.exception.ExtException;

public class AnnotatedException
    extends Exception
    implements ExtException
{
    private Throwable _underlyingException;

    AnnotatedException(String string, Throwable e)
    {
        super(string);

        _underlyingException = e;
    }

    AnnotatedException(String string)
    {
        this(string, null);
    }

    Throwable getUnderlyingException()
    {
        return _underlyingException;
    }

    public Throwable getCause()
    {
        return _underlyingException;
    }
}
