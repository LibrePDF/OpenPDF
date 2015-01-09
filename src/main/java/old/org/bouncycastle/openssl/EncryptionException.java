package old.org.bouncycastle.openssl;

import java.io.IOException;

public class EncryptionException
    extends IOException
{
    private Throwable cause;

    public EncryptionException(String msg)
    {
        super(msg);
    }

    public EncryptionException(String msg, Throwable ex)
    {
        super(msg);
        this.cause = ex;
    }

    public Throwable getCause()
    {
        return cause;
    }
}