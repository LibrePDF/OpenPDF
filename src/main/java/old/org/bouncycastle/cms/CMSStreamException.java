package old.org.bouncycastle.cms;

import java.io.IOException;

public class CMSStreamException
    extends IOException
{
    private final Throwable underlying;

    CMSStreamException(String msg)
    {
        super(msg);
        this.underlying = null;
    }

    CMSStreamException(String msg, Throwable underlying)
    {
        super(msg);
        this.underlying = underlying;
    }

    public Throwable getCause()
    {
        return underlying;
    }
}
