package old.org.bouncycastle.cms;

public class CMSAttributeTableGenerationException
    extends CMSRuntimeException
{
    Exception   e;

    public CMSAttributeTableGenerationException(
        String name)
    {
        super(name);
    }

    public CMSAttributeTableGenerationException(
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
