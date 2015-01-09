package old.org.bouncycastle.operator;

public class GenericKey
{
    private Object representation;

    public GenericKey(Object representation)
    {
        this.representation = representation;
    }

    public Object getRepresentation()
    {
        return representation;
    }
}
