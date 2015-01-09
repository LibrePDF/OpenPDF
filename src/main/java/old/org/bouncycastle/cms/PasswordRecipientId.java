package old.org.bouncycastle.cms;

public class PasswordRecipientId
    extends RecipientId
{
    /**
     * Construct a recipient ID of the password type.
     */
    public PasswordRecipientId()
    {
        super(password);
    }

    public int hashCode()
    {
        return password;
    }

    public boolean equals(
        Object o)
    {
        if (!(o instanceof PasswordRecipientId))
        {
            return false;
        }

        return true;
    }

    public boolean match(Object obj)
    {
        if (obj instanceof PasswordRecipientInformation)
        {
            return true;
        }
        
        return false;
    }
}
