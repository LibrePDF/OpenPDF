package old.org.bouncycastle.cms;

import old.org.bouncycastle.util.Arrays;

public class KEKRecipientId
    extends RecipientId
{
    private byte[] keyIdentifier;

    /**
     * Construct a recipient ID with the key identifier of a KEK recipient.
     *
     * @param keyIdentifier a subjectKeyId
     */
    public KEKRecipientId(byte[] keyIdentifier)
    {
        super(kek);

        this.keyIdentifier = keyIdentifier;
    }

    public int hashCode()
    {
        return Arrays.hashCode(keyIdentifier);
    }

    public boolean equals(
        Object o)
    {
        if (!(o instanceof KEKRecipientId))
        {
            return false;
        }

        KEKRecipientId id = (KEKRecipientId)o;

        return Arrays.areEqual(keyIdentifier, id.keyIdentifier);
    }

    public boolean match(Object obj)
    {
        if (obj instanceof byte[])
        {
            return Arrays.areEqual(keyIdentifier, (byte[])obj);
        }
        else if (obj instanceof KEKRecipientInformation)
        {
            return ((KEKRecipientInformation)obj).getRID().equals(this);
        }

        return false;
    }
}
