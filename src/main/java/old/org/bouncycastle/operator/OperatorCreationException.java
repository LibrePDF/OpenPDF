package old.org.bouncycastle.operator;

public class OperatorCreationException
    extends OperatorException
{
    public OperatorCreationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }

    public OperatorCreationException(String msg)
    {
        super(msg);
    }
}
