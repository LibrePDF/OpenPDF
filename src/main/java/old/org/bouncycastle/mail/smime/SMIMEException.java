package old.org.bouncycastle.mail.smime;

public class SMIMEException 
    extends Exception 
{
    Exception   e;

    public SMIMEException(
        String name)
    {
        super(name);
    }

    public SMIMEException(
        String name,
        Exception e)
    {
        super(name);

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
