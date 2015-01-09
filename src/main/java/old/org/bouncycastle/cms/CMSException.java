package old.org.bouncycastle.cms;

public class CMSException
    extends Exception
{
    Exception   e;

    public CMSException(
        String msg)
    {
        super(msg);
    }

    public CMSException(
        String msg,
        Exception e)
    {
        super(msg);

        this.e = e;
    }

    public Exception getUnderlyingException()
    {
        return e;
    }
    
    public Throwable getCause()
    {
        return e;
    }
}
